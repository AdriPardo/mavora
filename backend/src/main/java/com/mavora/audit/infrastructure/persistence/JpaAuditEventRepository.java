package com.mavora.audit.infrastructure.persistence;

import com.mavora.audit.domain.AuditEvent;
import com.mavora.audit.domain.AuditEventRepository;
import org.springframework.stereotype.Component;

@Component
public class JpaAuditEventRepository implements AuditEventRepository {

    private final AuditEventJpaRepository jpaRepository;

    public JpaAuditEventRepository(AuditEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void append(AuditEvent event) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.setId(event.id());
        entity.setOrganizationId(event.organization().map(id -> id.value()).orElse(null));
        entity.setActorUserId(event.actor().map(id -> id.value()).orElse(null));
        entity.setAction(event.action());
        entity.setResourceType(event.resourceType());
        entity.setResourceId(event.resourceId());
        entity.setMetadata(event.metadata());
        entity.setCreatedAt(event.createdAt());
        entity.setIp(event.ip());
        jpaRepository.save(entity);
    }
}
