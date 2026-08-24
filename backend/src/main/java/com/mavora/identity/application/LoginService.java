package com.mavora.identity.application;

import com.mavora.audit.domain.AuditEvent;
import com.mavora.audit.domain.AuditEventRepository;
import com.mavora.identity.domain.Email;
import com.mavora.identity.domain.InvalidCredentialsException;
import com.mavora.identity.domain.Session;
import com.mavora.identity.domain.SessionRepository;
import com.mavora.identity.domain.User;
import com.mavora.identity.domain.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordHasher passwordHasher;
    private final SessionTokenService sessionTokenService;
    private final AuditEventRepository auditEventRepository;
    private final Clock clock;
    private final Duration sessionTtl;
    private final String dummyPasswordHash;

    public LoginService(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            PasswordHasher passwordHasher,
            SessionTokenService sessionTokenService,
            AuditEventRepository auditEventRepository,
            Clock clock,
            @Value("${mavora.auth.session-ttl}") Duration sessionTtl
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordHasher = passwordHasher;
        this.sessionTokenService = sessionTokenService;
        this.auditEventRepository = auditEventRepository;
        this.clock = clock;
        this.sessionTtl = sessionTtl;
        this.dummyPasswordHash = passwordHasher.hash("mavora-timing-guard");
    }

    @Transactional
    public AuthenticationResult login(LoginCommand command) {
        String rawPassword = command.password() == null ? "" : command.password();
        Email email = parseEmailOrNull(command.email());
        User user = email == null ? null : userRepository.findByEmail(email.normalized()).orElse(null);

        boolean credentialsMatch;
        if (user == null) {
            passwordHasher.matches(rawPassword, dummyPasswordHash);
            credentialsMatch = false;
        } else {
            credentialsMatch = passwordHasher.matches(rawPassword, user.passwordHash());
        }

        if (!credentialsMatch || user == null || !user.isActive()) {
            throw new InvalidCredentialsException();
        }

        Instant now = clock.instant();
        IssuedSessionToken token = sessionTokenService.issue();
        Instant expiresAt = now.plus(sessionTtl);
        sessionRepository.save(Session.issue(
                user.id(),
                token.tokenHash(),
                expiresAt,
                now,
                command.ip(),
                truncate(command.userAgent(), 256)
        ));

        auditEventRepository.append(AuditEvent.of(
                null,
                user.id(),
                "account.logged_in",
                "user",
                user.id().value(),
                null,
                now,
                command.ip()
        ));

        return new AuthenticationResult(user.id(), token.rawToken(), expiresAt);
    }

    private static Email parseEmailOrNull(String raw) {
        try {
            return new Email(raw);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
