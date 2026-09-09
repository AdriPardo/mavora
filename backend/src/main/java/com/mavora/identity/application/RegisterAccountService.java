package com.mavora.identity.application;

import com.mavora.audit.domain.AuditEvent;
import com.mavora.audit.domain.AuditEventRepository;
import com.mavora.identity.domain.Email;
import com.mavora.identity.domain.EmailAlreadyRegisteredException;
import com.mavora.identity.domain.Session;
import com.mavora.identity.domain.SessionRepository;
import com.mavora.identity.domain.User;
import com.mavora.identity.domain.UserRepository;
import com.mavora.organization.domain.Organization;
import com.mavora.organization.domain.OrganizationMember;
import com.mavora.organization.domain.OrganizationMemberRepository;
import com.mavora.organization.domain.OrganizationRepository;
import com.mavora.organization.domain.OrganizationRole;
import com.mavora.organization.domain.OrganizationSlug;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterAccountService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final SessionRepository sessionRepository;
    private final PasswordHasher passwordHasher;
    private final SessionTokenService sessionTokenService;
    private final AuditEventRepository auditEventRepository;
    private final Clock clock;
    private final Duration sessionTtl;

    public RegisterAccountService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository memberRepository,
            SessionRepository sessionRepository,
            PasswordHasher passwordHasher,
            SessionTokenService sessionTokenService,
            AuditEventRepository auditEventRepository,
            Clock clock,
            @Value("${mavora.auth.session-ttl}") Duration sessionTtl
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.sessionRepository = sessionRepository;
        this.passwordHasher = passwordHasher;
        this.sessionTokenService = sessionTokenService;
        this.auditEventRepository = auditEventRepository;
        this.clock = clock;
        this.sessionTtl = sessionTtl;
    }

    @Transactional
    public AuthenticationResult register(RegisterAccountCommand command) {
        Email email = new Email(command.email());
        PasswordPolicy.validate(command.password());
        if (userRepository.existsByEmail(email.normalized())) {
            throw new EmailAlreadyRegisteredException();
        }

        Instant now = clock.instant();
        User user = User.register(UserId.generate(), email, passwordHasher.hash(command.password()), now);
        userRepository.save(user);

        Organization organization = Organization.create(
                OrganizationId.generate(),
                command.organizationName(),
                uniqueSlug(command.organizationName()),
                now
        );
        organizationRepository.save(organization);

        OrganizationMember owner = OrganizationMember.create(
                organization.id(),
                user.id(),
                OrganizationRole.OWNER,
                now
        );
        memberRepository.save(owner);

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
                organization.id(),
                user.id(),
                "account.registered",
                "organization",
                organization.id().value(),
                null,
                now,
                command.ip()
        ));

        return new AuthenticationResult(user.id(), token.rawToken(), expiresAt);
    }

    private String uniqueSlug(String organizationName) {
        String base = OrganizationSlug.fromName(organizationName);
        String slug = base;
        int attempts = 0;
        while (organizationRepository.existsBySlug(slug)) {
            slug = base + "-" + HexFormat.of().formatHex(randomSuffix()).substring(0, 4);
            attempts++;
            if (attempts > 12) {
                throw new IllegalStateException("Unable to allocate organization slug");
            }
        }
        return slug;
    }

    private static byte[] randomSuffix() {
        byte[] bytes = new byte[2];
        UUID uuid = UUID.randomUUID();
        long lsb = uuid.getLeastSignificantBits();
        bytes[0] = (byte) lsb;
        bytes[1] = (byte) (lsb >> 8);
        return bytes;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
