package com.mavora.approvals.infrastructure.persistence;

import com.mavora.approvals.domain.ApprovalRequest;
import com.mavora.approvals.domain.ApprovalRequestRepository;
import com.mavora.approvals.domain.ApprovalStatus;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaApprovalRequestRepository implements ApprovalRequestRepository {

    private final ApprovalRequestJpaRepository jpaRepository;

    public JpaApprovalRequestRepository(ApprovalRequestJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ApprovalRequest save(ApprovalRequest request) {
        ApprovalRequestEntity entity = toEntity(request);
        entity.markNew(!jpaRepository.existsById(request.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<ApprovalRequest> findById(UUID id, OrganizationId organizationId) {
        return jpaRepository.findByIdAndOrganizationId(id, organizationId.value()).map(this::toDomain);
    }

    @Override
    public List<ApprovalRequest> findPending(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationIdAndStatusOrderByCreatedAtDesc(
                organizationId.value(), ApprovalStatus.PENDING
        ).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ApprovalRequest> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countPending(OrganizationId organizationId) {
        return jpaRepository.countByOrganizationIdAndStatus(organizationId.value(), ApprovalStatus.PENDING);
    }

    private ApprovalRequest toDomain(ApprovalRequestEntity entity) {
        return ApprovalRequest.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getType(),
                entity.getSubjectType(),
                entity.getSubjectId(),
                entity.getStatus(),
                entity.getSummary(),
                entity.getPayloadJson(),
                entity.getCreatedAt(),
                entity.getDecidedAt(),
                entity.getDecidedBy() == null ? null : new UserId(entity.getDecidedBy()),
                entity.getDecisionNote(),
                entity.getVersion()
        );
    }

    private ApprovalRequestEntity toEntity(ApprovalRequest request) {
        ApprovalRequestEntity entity = new ApprovalRequestEntity();
        entity.setId(request.id());
        entity.setOrganizationId(request.organizationId().value());
        entity.setType(request.type());
        entity.setSubjectType(request.subjectType());
        entity.setSubjectId(request.subjectId());
        entity.setStatus(request.status());
        entity.setSummary(request.summary());
        entity.setPayloadJson(request.payloadJson());
        entity.setCreatedAt(request.createdAt());
        entity.setDecidedAt(request.decidedAt());
        entity.setDecidedBy(request.decidedBy() == null ? null : request.decidedBy().value());
        entity.setDecisionNote(request.decisionNote());
        entity.setVersion(request.version());
        return entity;
    }
}
