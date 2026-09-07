package com.mavora.instagram.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.agents.application.LlmCompletion;
import com.mavora.agents.application.LlmGateway;
import com.mavora.agents.domain.AgentType;
import com.mavora.company.application.CompanyProfileService;
import com.mavora.company.application.FetchedPage;
import com.mavora.company.application.WebsiteFetcher;
import com.mavora.instagram.domain.InstagramAccount;
import com.mavora.knowledge.domain.KnowledgeItem;
import com.mavora.knowledge.domain.KnowledgeItemRepository;
import com.mavora.knowledge.domain.KnowledgeKind;
import com.mavora.shared.domain.OrganizationId;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstagramBusinessImportService {

    static final String TASK_MARKER = "TASK=BUSINESS_IMPORT";

    private static final Logger log = LoggerFactory.getLogger(InstagramBusinessImportService.class);

    private final List<InstagramProfileReader> readers;
    private final CompanyProfileService companyProfileService;
    private final BrandBriefService brandBriefService;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final WebsiteFetcher websiteFetcher;
    private final LlmGateway llmGateway;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public InstagramBusinessImportService(
            List<InstagramProfileReader> readers,
            CompanyProfileService companyProfileService,
            BrandBriefService brandBriefService,
            KnowledgeItemRepository knowledgeItemRepository,
            WebsiteFetcher websiteFetcher,
            LlmGateway llmGateway,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.readers = readers;
        this.companyProfileService = companyProfileService;
        this.brandBriefService = brandBriefService;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.websiteFetcher = websiteFetcher;
        this.llmGateway = llmGateway;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public List<String> importFromConnectedAccount(
            InstagramAccount account,
            String accessToken,
            String graphVersion
    ) {
        OrganizationId organizationId = account.organizationId();
        InstagramProfileSnapshot snapshot = readerFor(account).read(account, accessToken, graphVersion);
        boolean liveWeb = account.provider() != com.mavora.instagram.domain.InstagramProvider.FAKE;
        String websiteText = liveWeb ? fetchWebsiteText(snapshot.website()) : null;
        ImportedBusinessDraft draft = analyze(organizationId, snapshot, websiteText);
        List<String> filled = new ArrayList<>();
        filled.addAll(companyProfileService.mergeFromInstagram(
                organizationId,
                draft.companyName(),
                draft.websiteUrl(),
                draft.description(),
                draft.market(),
                draft.productName(),
                draft.productDescription()
        ));
        filled.addAll(brandBriefService.mergeFromInstagram(
                organizationId,
                draft.voice(),
                draft.offer(),
                draft.cta(),
                draft.audience(),
                draft.extraNotes()
        ));
        knowledgeItemRepository.save(KnowledgeItem.create(
                organizationId,
                KnowledgeKind.FACT,
                "Perfil de Instagram @" + snapshot.username(),
                profileFactBody(snapshot, websiteText),
                "instagram:@" + snapshot.username(),
                75,
                clock.instant()
        ));
        filled.add("knowledge.instagram");
        if (liveWeb && snapshot.website() != null && !snapshot.website().isBlank()) {
            companyProfileService.ingestWebsiteQuietly(organizationId);
        }
        return filled;
    }

    private InstagramProfileReader readerFor(InstagramAccount account) {
        return readers.stream()
                .filter(reader -> reader.supports(account.provider()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Instagram profile reader for " + account.provider()));
    }

    private ImportedBusinessDraft analyze(
            OrganizationId organizationId,
            InstagramProfileSnapshot snapshot,
            String websiteText
    ) {
        ImportedBusinessDraft fallback = heuristic(snapshot, websiteText);
        try {
            LlmCompletion completion = llmGateway.complete(
                    organizationId,
                    AgentType.INSTAGRAM,
                    "Eres el analista de marca de Mavora. Extrae SOLO lo que el perfil de Instagram (y la web, si hay) "
                            + "demuestran. Si no está en la evidencia, deja el campo vacío. No inventes métricas, "
                            + "presupuesto, plazos ni resultados. Responde JSON.",
                    prompt(snapshot, websiteText)
            );
            JsonNode node = objectMapper.readTree(completion.content());
            return new ImportedBusinessDraft(
                    firstText(node, "companyName", fallback.companyName()),
                    firstText(node, "websiteUrl", fallback.websiteUrl()),
                    firstText(node, "description", fallback.description()),
                    firstText(node, "market", fallback.market()),
                    firstText(node, "productName", fallback.productName()),
                    firstText(node, "productDescription", fallback.productDescription()),
                    firstText(node, "voice", fallback.voice()),
                    firstText(node, "offer", fallback.offer()),
                    firstText(node, "cta", fallback.cta()),
                    firstText(node, "audience", fallback.audience()),
                    firstText(node, "extraNotes", fallback.extraNotes())
            );
        } catch (RuntimeException exception) {
            log.info("Instagram business import used heuristic: {}", exception.getMessage());
            return fallback;
        } catch (Exception exception) {
            log.info("Instagram business import used heuristic: {}", exception.getMessage());
            return fallback;
        }
    }

    private ImportedBusinessDraft heuristic(InstagramProfileSnapshot snapshot, String websiteText) {
        String name = snapshot.displayName();
        String bio = blankToNull(snapshot.biography());
        if (bio == null && snapshot.pageAbout() != null && !snapshot.pageAbout().isBlank()) {
            bio = snapshot.pageAbout().trim();
        }
        String website = blankToNull(snapshot.website());
        String market = blankToNull(snapshot.pageCategory());
        if (market == null) {
            market = "Instagram · @" + snapshot.username();
        }
        String extra = "Importado del perfil @" + snapshot.username() + ".";
        if (snapshot.followers() != null) {
            extra = extra + " Seguidores declarados: " + snapshot.followers() + ".";
        }
        extra = extra + " No es una promesa de algoritmo; es lo que el perfil dice de sí mismo.";
        if (websiteText != null && !websiteText.isBlank()) {
            extra = extra + " También leímos la web de la bio.";
        }
        return new ImportedBusinessDraft(
                name,
                website,
                bio,
                market,
                name,
                bio,
                bio == null ? "Directa, en primera persona, como el perfil de Instagram." : "Como el perfil: " + clip(bio, 180),
                bio,
                website == null ? "Escríbenos por DM." : "Enlace en la bio.",
                "Personas que ya siguen o descubren @" + snapshot.username(),
                extra
        );
    }

    private String prompt(InstagramProfileSnapshot snapshot, String websiteText) {
        StringBuilder builder = new StringBuilder();
        builder.append(TASK_MARKER).append('\n');
        builder.append("Username: @").append(snapshot.username()).append('\n');
        builder.append("Name: ").append(nullToEmpty(snapshot.name())).append('\n');
        builder.append("Biography: ").append(nullToEmpty(snapshot.biography())).append('\n');
        builder.append("Website: ").append(nullToEmpty(snapshot.website())).append('\n');
        builder.append("Followers: ").append(snapshot.followers() == null ? "" : snapshot.followers()).append('\n');
        builder.append("Page: ").append(nullToEmpty(snapshot.pageName())).append('\n');
        builder.append("Category: ").append(nullToEmpty(snapshot.pageCategory())).append('\n');
        builder.append("About: ").append(nullToEmpty(snapshot.pageAbout())).append('\n');
        builder.append("Captions:\n");
        for (String caption : snapshot.recentCaptions() == null ? List.<String>of() : snapshot.recentCaptions()) {
            builder.append("- ").append(caption).append('\n');
        }
        if (websiteText != null && !websiteText.isBlank()) {
            builder.append("Website text:\n").append(clip(websiteText, 2500)).append('\n');
        }
        builder.append("JSON keys: companyName, description, market, productName, productDescription, ");
        builder.append("voice, offer, cta, audience, extraNotes, websiteUrl\n");
        return builder.toString();
    }

    private String fetchWebsiteText(String website) {
        if (website == null || website.isBlank()) {
            return null;
        }
        try {
            FetchedPage page = websiteFetcher.fetch(website);
            return page.text();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static String profileFactBody(InstagramProfileSnapshot snapshot, String websiteText) {
        StringBuilder body = new StringBuilder();
        body.append("Cuenta @").append(snapshot.username());
        if (snapshot.name() != null && !snapshot.name().isBlank()) {
            body.append(" (").append(snapshot.name().trim()).append(')');
        }
        body.append(".\n");
        if (snapshot.biography() != null && !snapshot.biography().isBlank()) {
            body.append("Bio: ").append(snapshot.biography().trim()).append('\n');
        }
        if (snapshot.website() != null && !snapshot.website().isBlank()) {
            body.append("Web: ").append(snapshot.website().trim()).append('\n');
        }
        if (snapshot.followers() != null) {
            body.append("Seguidores: ").append(snapshot.followers()).append('\n');
        }
        if (snapshot.pageName() != null && !snapshot.pageName().isBlank()) {
            body.append("Página: ").append(snapshot.pageName().trim()).append('\n');
        }
        if (snapshot.recentCaptions() != null && !snapshot.recentCaptions().isEmpty()) {
            body.append("Últimos captions:\n");
            for (String caption : snapshot.recentCaptions()) {
                body.append("- ").append(caption).append('\n');
            }
        }
        if (websiteText != null && !websiteText.isBlank()) {
            body.append("Extracto web:\n").append(clip(websiteText, 1500));
        }
        return body.toString();
    }

    private static String firstText(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText("");
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return fallback;
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String clip(String value, int max) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
