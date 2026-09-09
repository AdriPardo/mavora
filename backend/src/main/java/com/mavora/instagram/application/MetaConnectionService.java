package com.mavora.instagram.application;

import com.mavora.instagram.domain.MetaConnectionSettings;
import com.mavora.instagram.domain.MetaConnectionSettingsRepository;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetaConnectionService {

    /**
     * Permisos que Mavora pide en OAuth. Solo Graph de la cuenta Professional y la
     * Página ligadas por el dueño: perfil, media propia y publicación. No pedimos
     * Instagram Public Content Access (hashtags / discovery de terceros), Marketing
     * API / Ads, ni {@code instagram_manage_insights}: no hay llamadas a {@code /insights};
     * la analítica es un snapshot que pega el usuario.
     */
    public static final List<String> REQUIRED_SCOPES = List.of(
            "instagram_basic",
            "instagram_content_publish",
            "pages_show_list",
            "pages_read_engagement"
    );

    private final OrganizationAuthorizationService authorizationService;
    private final MetaConnectionSettingsRepository settingsRepository;
    private final TokenProtector tokenProtector;
    private final Clock clock;
    private final String envAppId;
    private final String envAppSecret;
    private final String defaultRedirectUri;
    private final String defaultGraphVersion;
    private final String publicApiUrl;
    private final String publicAppUrl;

    public MetaConnectionService(
            OrganizationAuthorizationService authorizationService,
            MetaConnectionSettingsRepository settingsRepository,
            TokenProtector tokenProtector,
            Clock clock,
            @Value("${mavora.instagram.app-id:}") String envAppId,
            @Value("${mavora.instagram.app-secret:}") String envAppSecret,
            @Value("${mavora.instagram.redirect-uri:}") String envRedirectUri,
            @Value("${mavora.instagram.graph-version:v21.0}") String defaultGraphVersion,
            @Value("${mavora.public.api-url:http://localhost:8080}") String publicApiUrl,
            @Value("${mavora.public.app-url:http://localhost:5173}") String publicAppUrl
    ) {
        this.authorizationService = authorizationService;
        this.settingsRepository = settingsRepository;
        this.tokenProtector = tokenProtector;
        this.clock = clock;
        this.envAppId = blankToNull(envAppId);
        this.envAppSecret = blankToNull(envAppSecret);
        this.publicApiUrl = trimSlash(publicApiUrl);
        this.publicAppUrl = trimSlash(publicAppUrl);
        this.defaultRedirectUri = blankToNull(envRedirectUri) != null
                ? envRedirectUri.trim()
                : this.publicApiUrl + "/api/v1/integrations/instagram/callback";
        this.defaultGraphVersion = defaultGraphVersion == null || defaultGraphVersion.isBlank()
                ? "v21.0"
                : defaultGraphVersion.trim();
    }

    @Transactional(readOnly = true)
    public SetupView setup(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        Optional<MetaConnectionSettings> stored = settingsRepository.findByOrganization(organizationId);
        Optional<MetaAppCredentials> resolved = resolve(organizationId);
        String appId = stored.map(MetaConnectionSettings::appId).orElse("");
        boolean secretConfigured = stored.map(MetaConnectionSettings::isReady).orElse(false);
        String redirectUri = stored.map(MetaConnectionSettings::redirectUri).filter(value -> value != null && !value.isBlank())
                .orElse(defaultRedirectUri);
        String graphVersion = stored.map(MetaConnectionSettings::graphVersion).orElse(defaultGraphVersion);
        String source = resolved.map(MetaAppCredentials::source).orElse("none");
        return new SetupView(
                appId == null ? "" : appId,
                secretConfigured,
                redirectUri,
                graphVersion,
                resolved.isPresent(),
                source,
                publicApiUrl,
                publicAppUrl,
                defaultRedirectUri,
                publicAppUrl + "/privacidad",
                publicAppUrl + "/meta-app-icon.png",
                REQUIRED_SCOPES,
                "https://developers.facebook.com/apps/"
        );
    }

    @Transactional
    public SetupView save(
            OrganizationId organizationId,
            UserId userId,
            String appId,
            String appSecret,
            String redirectUri,
            String graphVersion
    ) {
        authorizationService.requireWriter(organizationId, userId);
        Instant now = clock.instant();
        String trimmedAppId = requireAppId(appId);
        String trimmedSecret = blankToNull(appSecret);
        MetaConnectionSettings settings = settingsRepository.findByOrganization(organizationId)
                .orElseGet(() -> MetaConnectionSettings.create(
                        organizationId, trimmedAppId, null, redirectUri, graphVersion, now
                ));
        String secretCiphertext = null;
        if (trimmedSecret != null) {
            if (trimmedSecret.length() < 8 || trimmedSecret.length() > 256) {
                throw new DomainException("El App Secret de Meta debe tener entre 8 y 256 caracteres");
            }
            secretCiphertext = tokenProtector.encrypt(trimmedSecret);
        } else if (!settings.isReady()) {
            throw new DomainException("Pega el App Secret de Meta. Solo se muestra una vez; no lo volvemos a devolver.");
        }
        settings.update(trimmedAppId, secretCiphertext, redirectUri, graphVersion, now);
        settingsRepository.save(settings);
        return setup(organizationId, userId);
    }

    @Transactional
    public SetupView clear(OrganizationId organizationId, UserId userId) {
        authorizationService.requireWriter(organizationId, userId);
        settingsRepository.findByOrganization(organizationId).ifPresent(settingsRepository::delete);
        return setup(organizationId, userId);
    }

    public Optional<MetaAppCredentials> resolve(OrganizationId organizationId) {
        Optional<MetaConnectionSettings> stored = settingsRepository.findByOrganization(organizationId);
        if (stored.isPresent() && stored.get().isReady()) {
            MetaConnectionSettings settings = stored.get();
            String redirect = settings.redirectUri() != null ? settings.redirectUri() : defaultRedirectUri;
            return Optional.of(new MetaAppCredentials(
                    settings.appId(),
                    tokenProtector.decrypt(settings.appSecretCiphertext()),
                    redirect,
                    settings.graphVersion(),
                    "organization"
            ));
        }
        if (envAppId != null && envAppSecret != null) {
            return Optional.of(new MetaAppCredentials(
                    envAppId, envAppSecret, defaultRedirectUri, defaultGraphVersion, "environment"
            ));
        }
        return Optional.empty();
    }

    private static String requireAppId(String appId) {
        String trimmed = blankToNull(appId);
        if (trimmed == null || trimmed.length() < 5 || trimmed.length() > 64) {
            throw new DomainException("El App ID de Meta es obligatorio (5–64 caracteres)");
        }
        if (!trimmed.matches("[0-9A-Za-z]+")) {
            throw new DomainException("El App ID de Meta solo admite letras y números");
        }
        return trimmed;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String trimSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    public record SetupView(
            String appId,
            boolean secretConfigured,
            String redirectUri,
            String graphVersion,
            boolean oauthReady,
            String source,
            String publicApiUrl,
            String publicAppUrl,
            String suggestedRedirectUri,
            String privacyPolicyUrl,
            String appIconUrl,
            List<String> scopes,
            String developerConsoleUrl
    ) {
    }
}
