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
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstagramAccountService {

    private final OrganizationAuthorizationService authorizationService;
    private final InstagramAccountRepository accountRepository;
    private final TokenProtector tokenProtector;
    private final ObjectProvider<InstagramOAuthClient> oauthClient;
    private final Clock clock;
    private final InstagramProvider configuredProvider;

    public InstagramAccountService(
            OrganizationAuthorizationService authorizationService,
            InstagramAccountRepository accountRepository,
            TokenProtector tokenProtector,
            ObjectProvider<InstagramOAuthClient> oauthClient,
            Clock clock,
            @Value("${mavora.instagram.provider:fake}") String provider
    ) {
        this.authorizationService = authorizationService;
        this.accountRepository = accountRepository;
        this.tokenProtector = tokenProtector;
        this.oauthClient = oauthClient;
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
        return account.map(this::toStatus).orElseGet(() -> new Status(
                configuredProvider.name().toLowerCase(),
                false,
                null,
                null,
                false,
                null,
                true
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
        return toStatus(saved);
    }

    @Transactional(readOnly = true)
    public ConnectUrl connectUrl(OrganizationId organizationId, UserId userId) {
        authorizationService.requireWriter(organizationId, userId);
        InstagramOAuthClient client = oauthClient.getIfAvailable();
        if (configuredProvider != InstagramProvider.META || client == null) {
            throw new DomainException(
                    "Instagram OAuth is not configured. Set INSTAGRAM_PROVIDER=meta and Meta app credentials, "
                            + "or use the demo connect in local/fake mode."
            );
        }
        Instant exp = clock.instant().plusSeconds(600);
        String payload = organizationId.value() + "." + exp.getEpochSecond();
        String state = payload + "." + tokenProtector.sign(payload);
        return new ConnectUrl(client.authorizeUrl(state), "meta");
    }

    @Transactional
    public OrganizationId completeOAuth(String code, String state) {
        InstagramOAuthClient client = oauthClient.getIfAvailable();
        if (configuredProvider != InstagramProvider.META || client == null) {
            throw new DomainException("Instagram OAuth is not configured");
        }
        OrganizationId organizationId = parseState(state);
        InstagramOAuthClient.ConnectedAccount connected = client.exchange(code);
        Instant now = clock.instant();
        upsert(
                organizationId,
                InstagramProvider.META,
                connected.igUserId(),
                connected.username(),
                connected.pageId(),
                tokenProtector.encrypt(connected.accessToken()),
                connected.expiresAt(),
                now
        );
        return organizationId;
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
        return new Status(
                account.provider().name().toLowerCase(),
                account.isConnected(),
                account.isConnected() ? account.username() : null,
                account.isConnected() ? account.igUserId() : null,
                account.isConnected() && account.autonomyEnabled(),
                account.isConnected() ? account.connectedAt() : null,
                true
        );
    }

    public record Status(
            String provider,
            boolean connected,
            String username,
            String igUserId,
            boolean autonomyEnabled,
            Instant connectedAt,
            boolean professionalAccountRequired
    ) {
    }

    public record ConnectUrl(String url, String provider) {
    }
}
