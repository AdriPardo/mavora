package com.mavora.instagram.application;

import com.mavora.instagram.domain.InstagramAccount;
import com.mavora.instagram.domain.InstagramAccountRepository;
import com.mavora.instagram.domain.InstagramProvider;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstagramAccountService {

    private static final Logger log = LoggerFactory.getLogger(InstagramAccountService.class);

    private final OrganizationAuthorizationService authorizationService;
    private final InstagramAccountRepository accountRepository;
    private final TokenProtector tokenProtector;
    private final InstagramOAuthClient oauthClient;
    private final MetaConnectionService metaConnectionService;
    private final InstagramBusinessImportService businessImportService;
    private final Clock clock;
    private final InstagramProvider configuredProvider;

    public InstagramAccountService(
            OrganizationAuthorizationService authorizationService,
            InstagramAccountRepository accountRepository,
            TokenProtector tokenProtector,
            InstagramOAuthClient oauthClient,
            MetaConnectionService metaConnectionService,
            InstagramBusinessImportService businessImportService,
            Clock clock,
            @Value("${mavora.instagram.provider:fake}") String provider
    ) {
        this.authorizationService = authorizationService;
        this.accountRepository = accountRepository;
        this.tokenProtector = tokenProtector;
        this.oauthClient = oauthClient;
        this.metaConnectionService = metaConnectionService;
        this.businessImportService = businessImportService;
        this.clock = clock;
        this.configuredProvider = InstagramProvider.valueOf(provider.trim().toUpperCase());
    }

    @Transactional(readOnly = true)
    public Status status(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return statusInternal(organizationId);
    }

    public Status statusInternal(OrganizationId organizationId) {
        Optional<InstagramAccount> account = accountRepository.findByOrganization(organizationId);
        boolean oauthReady = metaConnectionService.resolve(organizationId).isPresent();
        return account.map(found -> toStatus(found, oauthReady)).orElseGet(() -> new Status(
                configuredProvider.name().toLowerCase(),
                false,
                null,
                null,
                false,
                null,
                true,
                oauthReady,
                List.of(),
                null
        ));
    }

    @Transactional
    public Status connectFake(OrganizationId organizationId, UserId userId, String username) {
        authorizationService.requireWriter(organizationId, userId);
        if (configuredProvider != InstagramProvider.FAKE) {
            throw new DomainException("Fake Instagram connect is disabled when META provider is configured");
        }
        Instant now = clock.instant();
        String ciphertext = tokenProtector.encrypt("fake-token-" + organizationId);
        InstagramAccount saved = upsert(
                organizationId,
                InstagramProvider.FAKE,
                "fake-" + UUID.randomUUID(),
                username,
                null,
                ciphertext,
                now.plusSeconds(60L * 60 * 24 * 365),
                now
        );
        return toStatus(importQuietly(saved));
    }

    @Transactional(readOnly = true)
    public ConnectUrl connectUrl(OrganizationId organizationId, UserId userId) {
        authorizationService.requireWriter(organizationId, userId);
        MetaAppCredentials credentials = metaConnectionService.resolve(organizationId)
                .orElseThrow(() -> new DomainException(
                        "Guarda el App ID y el App Secret de Meta en Integraciones antes de conectar Instagram."
                ));
        Instant exp = clock.instant().plusSeconds(600);
        String payload = organizationId.value() + "." + exp.getEpochSecond();
        String state = payload + "." + tokenProtector.sign(payload);
        return new ConnectUrl(oauthClient.authorizeUrl(credentials, state), "meta");
    }

    @Transactional
    public OrganizationId completeOAuth(String code, String state) {
        OrganizationId organizationId = parseState(state);
        MetaAppCredentials credentials = metaConnectionService.resolve(organizationId)
                .orElseThrow(() -> new DomainException("Instagram OAuth is not configured"));
        InstagramOAuthClient.ConnectedAccount connected = oauthClient.exchange(credentials, code);
        Instant now = clock.instant();
        InstagramAccount saved = upsert(
                organizationId,
                InstagramProvider.META,
                connected.igUserId(),
                connected.username(),
                connected.pageId(),
                tokenProtector.encrypt(connected.accessToken()),
                connected.expiresAt(),
                now
        );
        importQuietly(saved);
        return organizationId;
    }

    @Transactional
    public Status connectWithToken(
            OrganizationId organizationId,
            UserId userId,
            String username,
            String igUserId,
            String pageId,
            String accessToken
    ) {
        authorizationService.requireWriter(organizationId, userId);
        if (accessToken == null || accessToken.isBlank() || accessToken.length() < 20) {
            throw new DomainException("Pega un token de página de Meta (mínimo 20 caracteres)");
        }
        if (igUserId == null || igUserId.isBlank()) {
            throw new DomainException("El Instagram Business Account ID es obligatorio");
        }
        Instant now = clock.instant();
        InstagramAccount saved = upsert(
                organizationId,
                InstagramProvider.META,
                igUserId.trim(),
                username,
                pageId,
                tokenProtector.encrypt(accessToken.trim()),
                now.plusSeconds(60L * 60 * 24 * 60),
                now
        );
        return toStatus(importQuietly(saved), true);
    }

    @Transactional
    public Status setAutonomy(OrganizationId organizationId, UserId userId, boolean enabled) {
        authorizationService.requireWriter(organizationId, userId);
        InstagramAccount account = accountRepository.findByOrganization(organizationId)
                .orElseThrow(() -> new DomainException("Connect Instagram first"));
        account.setAutonomy(enabled);
        return toStatus(accountRepository.save(account));
    }

    @Transactional
    public Status disconnect(OrganizationId organizationId, UserId userId) {
        authorizationService.requireWriter(organizationId, userId);
        InstagramAccount account = accountRepository.findByOrganization(organizationId)
                .orElseThrow(() -> new DomainException("Connect Instagram first"));
        account.disconnect(clock.instant());
        return toStatus(accountRepository.save(account));
    }

    private InstagramAccount importQuietly(InstagramAccount account) {
        try {
            String token = tokenProtector.decrypt(account.tokenCiphertext());
            String graphVersion = metaConnectionService.resolve(account.organizationId())
                    .map(MetaAppCredentials::graphVersion)
                    .orElse("v21.0");
            List<String> filled = businessImportService.importFromConnectedAccount(account, token, graphVersion);
            String summary = filled.isEmpty()
                    ? "Perfil leído; no había huecos que rellenar."
                    : "Rellenamos " + filled.size() + " campos desde @" + account.username() + ".";
            account.recordImport(summary, filled, clock.instant());
            return accountRepository.save(account);
        } catch (RuntimeException exception) {
            log.warn("Instagram profile import skipped: {}", exception.getMessage());
            return account;
        }
    }

    public String decryptToken(InstagramAccount account) {
        if (!account.isConnected()) {
            throw new DomainException("Instagram is not connected");
        }
        return tokenProtector.decrypt(account.tokenCiphertext());
    }

    private InstagramAccount upsert(
            OrganizationId organizationId,
            InstagramProvider provider,
            String igUserId,
            String username,
            String pageId,
            String tokenCiphertext,
            Instant expiresAt,
            Instant now
    ) {
        return accountRepository.findByOrganization(organizationId)
                .map(existing -> {
                    existing.reconnect(provider, igUserId, username, pageId, tokenCiphertext, expiresAt, now);
                    return accountRepository.save(existing);
                })
                .orElseGet(() -> accountRepository.save(InstagramAccount.connect(
                        organizationId, provider, igUserId, username, pageId, tokenCiphertext, expiresAt, now
                )));
    }

    private OrganizationId parseState(String state) {
        if (state == null || state.isBlank()) {
            throw new DomainException(DomainException.ErrorType.FORBIDDEN, "Invalid OAuth state", "OAuth state is missing");
        }
        String[] parts = state.split("\\.");
        if (parts.length != 3) {
            throw new DomainException(DomainException.ErrorType.FORBIDDEN, "Invalid OAuth state", "OAuth state is malformed");
        }
        String payload = parts[0] + "." + parts[1];
        if (!tokenProtector.verify(payload, parts[2])) {
            throw new DomainException(DomainException.ErrorType.FORBIDDEN, "Invalid OAuth state", "OAuth state signature is invalid");
        }
        long exp = Long.parseLong(parts[1]);
        if (clock.instant().getEpochSecond() > exp) {
            throw new DomainException(DomainException.ErrorType.FORBIDDEN, "Invalid OAuth state", "OAuth state has expired");
        }
        return OrganizationId.from(parts[0]);
    }

    private Status toStatus(InstagramAccount account) {
        return toStatus(account, metaConnectionService.resolve(account.organizationId()).isPresent());
    }

    private Status toStatus(InstagramAccount account, boolean oauthReady) {
        return new Status(
                account.provider().name().toLowerCase(),
                account.isConnected(),
                account.isConnected() ? account.username() : null,
                account.isConnected() ? account.igUserId() : null,
                account.isConnected() && account.autonomyEnabled(),
                account.isConnected() ? account.connectedAt() : null,
                true,
                oauthReady,
                account.isConnected() ? account.filledFromProfile() : List.of(),
                account.isConnected() ? account.importSummary() : null
        );
    }

    public record Status(
            String provider,
            boolean connected,
            String username,
            String igUserId,
            boolean autonomyEnabled,
            Instant connectedAt,
            boolean professionalAccountRequired,
            boolean oauthReady,
            List<String> filledFromProfile,
            String profileSummary
    ) {
    }

    public record ConnectUrl(String url, String provider) {
    }
}
