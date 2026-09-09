package com.mavora.identity.application;

import com.mavora.audit.domain.AuditEvent;
import com.mavora.audit.domain.AuditEventRepository;
import com.mavora.identity.domain.Session;
import com.mavora.identity.domain.SessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService {

    private final SessionRepository sessionRepository;
    private final SessionTokenService sessionTokenService;
    private final AuditEventRepository auditEventRepository;
    private final Clock clock;

    public LogoutService(
            SessionRepository sessionRepository,
            SessionTokenService sessionTokenService,
            AuditEventRepository auditEventRepository,
            Clock clock
    ) {
        this.sessionRepository = sessionRepository;
        this.sessionTokenService = sessionTokenService;
        this.auditEventRepository = auditEventRepository;
        this.clock = clock;
    }

    @Transactional
    public void logout(String rawSessionToken, String ip) {
        if (rawSessionToken == null || rawSessionToken.isBlank()) {
            return;
        }
        Instant now = clock.instant();
        Optional<Session> session = sessionRepository.findActiveByTokenHash(sessionTokenService.hash(rawSessionToken), now);
        session.ifPresent(active -> {
            active.revoke(now);
            sessionRepository.save(active);
            auditEventRepository.append(AuditEvent.of(
                    null,
                    active.userId(),
                    "account.logged_out",
                    "user",
                    active.userId().value(),
                    null,
                    now,
                    ip
            ));
        });
    }
}
