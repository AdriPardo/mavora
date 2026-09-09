package com.mavora.instagram.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.agents.application.HandlerResult;
import com.mavora.agents.application.LlmCompletion;
import com.mavora.agents.application.LlmGateway;
import com.mavora.agents.application.WorkflowHandler;
import com.mavora.agents.domain.AgentType;
import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowType;
import com.mavora.company.domain.Company;
import com.mavora.company.domain.CompanyRepository;
import com.mavora.company.domain.Product;
import com.mavora.company.domain.ProductRepository;
import com.mavora.generation.application.MediaGenerator;
import com.mavora.instagram.domain.BrandBrief;
import com.mavora.instagram.domain.BrandBriefRepository;
import com.mavora.instagram.domain.InstagramAccount;
import com.mavora.instagram.domain.InstagramAccountRepository;
import com.mavora.instagram.domain.InstagramFormat;
import com.mavora.instagram.domain.InstagramPlaybook;
import com.mavora.instagram.domain.InstagramSlot;
import com.mavora.instagram.domain.InstagramSlotRepository;
import com.mavora.instagram.domain.MediaAsset;
import com.mavora.instagram.domain.MediaAssetRepository;
import com.mavora.instagram.domain.MediaKind;
import com.mavora.knowledge.domain.KnowledgeItem;
import com.mavora.knowledge.domain.KnowledgeItemRepository;
import com.mavora.knowledge.domain.KnowledgeKind;
import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class InstagramWeekHandler implements WorkflowHandler {

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final BrandBriefRepository briefRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final InstagramAccountRepository accountRepository;
    private final InstagramSlotRepository slotRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final InstagramPublishService publishService;
    private final MediaLibraryService mediaLibraryService;
    private final MediaGenerator mediaGenerator;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public InstagramWeekHandler(
            CompanyRepository companyRepository,
            ProductRepository productRepository,
            BrandBriefRepository briefRepository,
            MediaAssetRepository mediaAssetRepository,
            InstagramAccountRepository accountRepository,
            InstagramSlotRepository slotRepository,
            KnowledgeItemRepository knowledgeItemRepository,
            InstagramPublishService publishService,
            MediaLibraryService mediaLibraryService,
            MediaGenerator mediaGenerator,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.briefRepository = briefRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.accountRepository = accountRepository;
        this.slotRepository = slotRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.publishService = publishService;
        this.mediaLibraryService = mediaLibraryService;
        this.mediaGenerator = mediaGenerator;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public boolean supports(WorkflowType type) {
        return type == WorkflowType.INSTAGRAM_WEEK;
    }

    @Override
    public HandlerResult execute(WorkflowExecution execution, LlmGateway llm) {
        Company company = companyRepository.findByOrganization(execution.organizationId())
                .orElseThrow(() -> new DomainException("Connect the company before planning Instagram"));
        InstagramAccount account = accountRepository.findByOrganization(execution.organizationId())
                .filter(InstagramAccount::isConnected)
                .orElseThrow(() -> new DomainException("Connect Instagram before generating the week"));
        List<MediaAsset> assets = mediaAssetRepository.findByOrganization(execution.organizationId());
        List<MediaAsset> images = assets.stream().filter(MediaAsset::isImage).toList();
        List<MediaAsset> videos = assets.stream().filter(MediaAsset::isVideo).toList();
        List<Product> products = productRepository.findByCompany(company.id());
        BrandBrief brief = briefRepository.findByOrganization(execution.organizationId()).orElse(null);

        String system = """
                Eres el social lead de Instagram de Mavora. Devuelve JSON válido:
                copies[{format,hook,caption,cta,hashtags[],visualPrompt}].
                format ∈ FEED|REEL|STORY|CAROUSEL.
                visualPrompt es una descripción en inglés para generar la imagen o el vídeo (Fal.ai).
                Optimiza para el algoritmo (hook, retención, guardados, CTA de venta) sin prometer resultados.
                Español de España para hook/caption/cta. 3–8 hashtags de nicho. CTA hacia bio, DM u oferta.
                """.stripIndent();
        String user = buildPrompt(company, products, brief, assets, account.username());
        LlmCompletion completion = llm.complete(execution.organizationId(), AgentType.INSTAGRAM, system, user);

        slotRepository.findScheduled(execution.organizationId()).forEach(slot -> {
            slot.cancel();
            slotRepository.save(slot);
        });

        List<Copy> copies = parseCopies(completion.content());
        Instant now = clock.instant();
        ZonedDateTime madrid = now.atZone(InstagramPlaybook.ZONE);
        LocalDate start = madrid.toLocalDate();
        List<InstagramSlot> created = new ArrayList<>();
        int index = 0;
        for (var blueprint : InstagramPlaybook.weekMix()) {
            ZonedDateTime when = start.with(blueprint.day()).atTime(blueprint.time()).atZone(InstagramPlaybook.ZONE);
            if (when.toInstant().isBefore(now) || when.toLocalDate().isBefore(start)) {
                when = when.plusWeeks(1);
            }
            Instant scheduledAt = index == 0 ? now : when.toInstant();
            Copy copy = pickCopy(copies, blueprint.format(), index, company, brief);
            List<UUID> mediaIds = visualsFor(
                    execution.organizationId(),
                    blueprint.format(),
                    copy,
                    brandContext(company, brief, products),
                    images,
                    videos,
                    index
            );
            created.add(slotRepository.save(InstagramSlot.schedule(
                    execution.organizationId(),
                    blueprint.format(),
                    scheduledAt,
                    copy.hook(),
                    copy.caption(),
                    copy.cta(),
                    copy.hashtags(),
                    mediaIds,
                    now
            )));
            index++;
        }

        knowledgeItemRepository.save(KnowledgeItem.create(
                execution.organizationId(),
                KnowledgeKind.DECISION,
                "Calendario Instagram autónomo",
                created.size() + " piezas planificadas para @" + account.username()
                        + " (reels, historias, feed y carruseles) sin aprobación humana. "
                        + InstagramPlaybook.PRINCIPLES,
                "instagram-week",
                80,
                now
        ));

        if (account.canAutoPublish()) {
            publishService.processDue();
        }

        try {
            return HandlerResult.of(
                    objectMapper.writeValueAsString(objectMapper.createObjectNode()
                            .put("slots", created.size())
                            .put("username", account.username())
                            .put("autonomy", account.canAutoPublish())),
                    completion
            );
        } catch (Exception exception) {
            throw new DomainException("Could not serialize Instagram week output");
        }
    }

    private String buildPrompt(
            Company company,
            List<Product> products,
            BrandBrief brief,
            List<MediaAsset> assets,
            String username
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append("Company: ").append(company.name()).append('\n');
        builder.append("Empresa: ").append(company.name()).append('\n');
        if (company.description() != null) {
            builder.append("Description: ").append(company.description()).append('\n');
        }
        if (company.market() != null) {
            builder.append("Market: ").append(company.market()).append('\n');
        }
        builder.append("Instagram: @").append(username).append('\n');
        if (brief != null) {
            builder.append("Voice: ").append(nullToEmpty(brief.voice())).append('\n');
            builder.append("Offer: ").append(nullToEmpty(brief.offer())).append('\n');
            builder.append("CTA: ").append(nullToEmpty(brief.cta())).append('\n');
            builder.append("Audience: ").append(nullToEmpty(brief.audience())).append('\n');
            builder.append("Notes: ").append(nullToEmpty(brief.extraNotes())).append('\n');
        }
        builder.append("Products:\n");
        for (Product product : products) {
            builder.append("- ").append(product.name());
            if (product.description() != null) {
                builder.append(": ").append(product.description());
            }
            builder.append('\n');
        }
        builder.append("Assets:\n");
        for (MediaAsset asset : assets) {
            builder.append("- ").append(asset.id()).append(" ").append(asset.kind()).append(" ")
                    .append(asset.filename());
            if (asset.captionHint() != null) {
                builder.append(" hint=").append(asset.captionHint());
            }
            builder.append('\n');
        }
        builder.append("Playbook:\n").append(InstagramPlaybook.PRINCIPLES);
        return builder.toString();
    }

    private List<Copy> parseCopies(String json) {
        List<Copy> copies = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            for (JsonNode node : root.path("copies")) {
                InstagramFormat format = InstagramFormat.valueOf(node.path("format").asText("FEED").toUpperCase(Locale.ROOT));
                List<String> tags = new ArrayList<>();
                node.path("hashtags").forEach(tag -> tags.add(normalizeHashtag(tag.asText())));
                copies.add(new Copy(
                        format,
                        node.path("hook").asText("Para si esto te suena."),
                        node.path("caption").asText("Publicamos para atraer visitas, seguidores y conversiones."),
                        node.path("cta").asText("Entra en el enlace de la bio."),
                        tags,
                        node.path("visualPrompt").asText("")
                ));
            }
        } catch (Exception ignored) {
            // Fallback templates below.
        }
        return copies;
    }

    private Copy pickCopy(List<Copy> copies, InstagramFormat format, int index, Company company, BrandBrief brief) {
        for (int i = 0; i < copies.size(); i++) {
            if (copies.get(i).format() == format) {
                return copies.remove(i);
            }
        }
        if (!copies.isEmpty()) {
            return copies.remove(0);
        }
        String offer = brief != null && brief.offer() != null ? brief.offer() : company.name();
        String cta = brief != null && brief.cta() != null ? brief.cta() : "Escríbenos por DM o entra en el enlace de la bio.";
        String hook = switch (format) {
            case REEL -> "Si tu marketing empieza de cero cada lunes, mira esto.";
            case STORY -> "¿Te falta un sistema (no otro chatbot)?";
            case CAROUSEL -> "Guarda esto: cómo pasar de visitas a venta.";
            case FEED -> company.name() + " no publica por publicar.";
        };
        String caption = offer + " Lo diseñamos para el algoritmo: hook, ritmo y una sola llamada a la acción. "
                + company.name() + " convierte atención en pipeline.";
        return new Copy(
                format,
                hook,
                caption,
                cta,
                List.of("#" + slug(company.name()), "#marketing", "#pymes"),
                defaultVisual(format, company)
        );
    }

    private List<UUID> visualsFor(
            OrganizationId organizationId,
            InstagramFormat format,
            Copy copy,
            String brandContext,
            List<MediaAsset> images,
            List<MediaAsset> videos,
            int index
    ) {
        int count = format == InstagramFormat.CAROUSEL ? 2 : 1;
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String prompt = copy.visualPrompt();
            if (prompt == null || prompt.isBlank()) {
                prompt = defaultVisual(format, null);
            }
            if (i > 0) {
                prompt = prompt + " Slide " + (i + 1) + ", complementary composition.";
            }
            MediaKind kind = format == InstagramFormat.REEL ? MediaKind.VIDEO : MediaKind.IMAGE;
            String aspect = (format == InstagramFormat.STORY || format == InstagramFormat.REEL) ? "9:16" : "4:5";
            try {
                MediaGenerator.GeneratedMedia generated = mediaGenerator.generate(
                        new MediaGenerator.GenerateCommand(kind, prompt, aspect, brandContext)
                );
                MediaAsset stored = mediaLibraryService.storeGenerated(
                        organizationId,
                        generated.kind(),
                        generated.filename(),
                        generated.contentType(),
                        generated.bytes(),
                        generated.prompt()
                );
                ids.add(stored.id());
            } catch (RuntimeException exception) {
                UUID fallback = fallbackAsset(format, images, videos, index + i);
                if (fallback == null) {
                    throw new DomainException("Could not generate or attach media for the Instagram week");
                }
                ids.add(fallback);
            }
        }
        if (format == InstagramFormat.CAROUSEL && ids.size() == 1 && !images.isEmpty()) {
            ids.add(images.get(index % images.size()).id());
        }
        return ids;
    }

    private static UUID fallbackAsset(
            InstagramFormat format,
            List<MediaAsset> images,
            List<MediaAsset> videos,
            int index
    ) {
        if (format == InstagramFormat.REEL && !videos.isEmpty()) {
            return videos.get(index % videos.size()).id();
        }
        if (images.isEmpty()) {
            return null;
        }
        return images.get(index % images.size()).id();
    }

    private static String brandContext(Company company, BrandBrief brief, List<Product> products) {
        StringBuilder builder = new StringBuilder(company.name());
        if (company.description() != null) {
            builder.append(". ").append(company.description());
        }
        if (brief != null && brief.offer() != null) {
            builder.append(". Offer: ").append(brief.offer());
        }
        for (Product product : products) {
            builder.append(". Product ").append(product.name());
        }
        return builder.toString();
    }

    private static String defaultVisual(InstagramFormat format, Company company) {
        String name = company == null ? "the brand" : company.name();
        return switch (format) {
            case REEL -> "Vertical 9:16 product reel, kinetic camera, clean UI screens, " + name + ", no watermark.";
            case STORY -> "Vertical 9:16 Instagram story, bold typography space, lifestyle, " + name + ".";
            case CAROUSEL -> "Portrait 4:5 carousel slide, educational layout, " + name + ", high contrast.";
            case FEED -> "Portrait 4:5 Instagram feed photo, social proof, " + name + ", natural light.";
        };
    }

    private static String normalizeHashtag(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isBlank()) {
            return "#mavora";
        }
        if (!value.startsWith("#")) {
            value = "#" + value;
        }
        return value.replaceAll("\\s+", "");
    }

    private static String slug(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "").transform(value ->
                value.isBlank() ? "marca" : value.substring(0, Math.min(20, value.length()))
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record Copy(
            InstagramFormat format,
            String hook,
            String caption,
            String cta,
            List<String> hashtags,
            String visualPrompt
    ) {
    }
}
