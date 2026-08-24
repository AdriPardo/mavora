package com.mavora.identity.infrastructure.security;

import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.identity.application.SessionTokenService;
import com.mavora.identity.domain.SessionRepository;
import com.mavora.identity.domain.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SessionTokenService sessionTokenService;
    private final Clock clock;
    private final String cookieName;

    public SessionAuthenticationFilter(
            SessionRepository sessionRepository,
            UserRepository userRepository,
            SessionTokenService sessionTokenService,
            Clock clock,
            String cookieName
    ) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.sessionTokenService = sessionTokenService;
        this.clock = clock;
        this.cookieName = cookieName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            readRawToken(request).ifPresent(this::authenticate);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String rawToken) {
        sessionRepository.findActiveByTokenHash(sessionTokenService.hash(rawToken), clock.instant())
                .flatMap(session -> userRepository.findById(session.userId()))
                .filter(user -> user.isActive())
                .ifPresent(user -> {
                    MavoraPrincipal principal = new MavoraPrincipal(user.id(), user.email().normalized());
                    var authentication = UsernamePasswordAuthenticationToken.authenticated(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                });
    }

    private Optional<String> readRawToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
