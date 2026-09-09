package com.mavora.identity.api;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieFactory {

    private final String cookieName;
    private final boolean secure;
    private final String sameSite;

    public AuthCookieFactory(
            @Value("${mavora.auth.cookie-name}") String cookieName,
            @Value("${mavora.auth.cookie-secure}") boolean secure,
            @Value("${mavora.auth.cookie-same-site}") String sameSite
    ) {
        this.cookieName = cookieName;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public String cookieName() {
        return cookieName;
    }

    public ResponseCookie create(String rawToken, Duration maxAge) {
        return base(rawToken, maxAge).build();
    }

    public ResponseCookie clear() {
        return base("", Duration.ZERO).build();
    }

    public <T> ResponseEntity<T> withSession(T body, String rawToken, Duration maxAge) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, create(rawToken, maxAge).toString())
                .body(body);
    }

    private ResponseCookie.ResponseCookieBuilder base(String value, Duration maxAge) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAge);
    }
}
