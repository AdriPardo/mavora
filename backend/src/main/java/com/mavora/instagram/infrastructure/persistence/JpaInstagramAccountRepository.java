package com.mavora.instagram.infrastructure.persistence;

import com.mavora.instagram.domain.InstagramAccount;
import com.mavora.instagram.domain.InstagramAccountRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaInstagramAccountRepository implements InstagramAccountRepository {

    private final InstagramAccountJpaRepository jpaRepository;

    public JpaInstagramAccountRepository(InstagramAccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public InstagramAccount save(InstagramAccount account) {
        InstagramAccountEntity entity = jpaRepository.findById(account.id()).orElseGet(InstagramAccountEntity::new);
        boolean creating = entity.getId() == null;
        copy(account, entity);
        entity.markNew(creating);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<InstagramAccount> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationId(organizationId.value())
                .map(JpaInstagramAccountRepository::toDomain);
    }

    static InstagramAccount toDomain(InstagramAccountEntity entity) {
        return InstagramAccount.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getProvider(),
                entity.getIgUserId(),
                entity.getUsername(),
                entity.getPageId(),
                entity.getTokenCiphertext(),
                entity.getTokenExpiresAt(),
                entity.isAutonomyEnabled(),
                entity.getConnectedAt(),
                entity.getDisconnectedAt(),
                entity.getImportSummary(),
                entity.getImportFields(),
                entity.getImportedAt(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }

    static void copy(InstagramAccount account, InstagramAccountEntity entity) {
        entity.setId(account.id());
        entity.setOrganizationId(account.organizationId().value());
        entity.setProvider(account.provider());
        entity.setIgUserId(account.igUserId());
        entity.setUsername(account.username());
        entity.setPageId(account.pageId());
        entity.setTokenCiphertext(account.tokenCiphertext());
        entity.setTokenExpiresAt(account.tokenExpiresAt());
        entity.setAutonomyEnabled(account.autonomyEnabled());
        entity.setConnectedAt(account.connectedAt());
        entity.setDisconnectedAt(account.disconnectedAt());
        entity.setImportSummary(account.importSummary());
        entity.setImportFields(account.importFields());
        entity.setImportedAt(account.importedAt());
        entity.setCreatedAt(account.createdAt());
        entity.setVersion(account.version());
    }
}
