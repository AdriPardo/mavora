package com.mavora.instagram.infrastructure.profile;

import com.mavora.instagram.application.InstagramProfileReader;
import com.mavora.instagram.application.InstagramProfileSnapshot;
import com.mavora.instagram.domain.InstagramAccount;
import com.mavora.instagram.domain.InstagramProvider;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FakeInstagramProfileReader implements InstagramProfileReader {

    @Override
    public boolean supports(InstagramProvider provider) {
        return provider == InstagramProvider.FAKE;
    }

    @Override
    public InstagramProfileSnapshot read(InstagramAccount account, String accessToken, String graphVersion) {
        String username = account.username();
        if ("acme.demo".equalsIgnoreCase(username)) {
            return new InstagramProfileSnapshot(
                    username,
                    "Acme Demo",
                    "Analítica y marketing para micro-SaaS. Publicamos prueba social, oferta clara y un solo CTA.",
                    "https://example.com",
                    1280,
                    42,
                    "Acme Demo",
                    "Software / SaaS",
                    "Herramienta de analítica para equipos pequeños que venden B2B en España.",
                    List.of(
                            "De visita a cliente: hook, dolor, oferta, prueba, CTA.",
                            "Si improvisas el feed cada lunes, este sistema es para ti.",
                            "El alcance no paga facturas. La oferta sí."
                    )
            );
        }
        String pretty = new InstagramProfileSnapshot(
                username, null, null, null, null, null, null, null, null, List.of()
        ).displayName();
        return new InstagramProfileSnapshot(
                username,
                pretty,
                "Perfil profesional de @" + username + " en Instagram.",
                null,
                null,
                null,
                pretty,
                null,
                null,
                List.of()
        );
    }
}
