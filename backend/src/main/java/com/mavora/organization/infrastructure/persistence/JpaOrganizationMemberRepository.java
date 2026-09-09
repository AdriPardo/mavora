package com.mavora.organization.infrastructure.persistence;

import com.mavora.organization.domain.OrganizationMember;
import com.mavora.organization.domain.OrganizationMemberRepository;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaOrganizationMemberRepository implements OrganizationMemberRepository {

    private final OrganizationMemberJpaRepository jpaRepository;

    public JpaOrganizationMemberRepository(OrganizationMemberJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrganizationMember save(OrganizationMember member) {
        OrganizationMemberEntity entity = toEntity(member);
        entity.markNew(!jpaRepository.existsById(member.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<OrganizationMember> findByOrganizationAndUser(OrganizationId organizationId, UserId userId) {
        return jpaRepository.findByOrganizationIdAndUserId(organizationId.value(), userId.value())
                .map(JpaOrganizationMemberRepository::toDomain);
    }

    @Override
    public List<OrganizationMember> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationId(organizationId.value()).stream()
                .map(JpaOrganizationMemberRepository::toDomain)
                .toList();
    }

    @Override
    public List<OrganizationMember> findByUser(UserId userId) {
        return jpaRepository.findByUserId(userId.value()).stream()
                .map(JpaOrganizationMemberRepository::toDomain)
                .toList();
    }

    static OrganizationMember toDomain(OrganizationMemberEntity entity) {
        return OrganizationMember.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                new UserId(entity.getUserId()),
                entity.getRole(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }

    static OrganizationMemberEntity toEntity(OrganizationMember member) {
        OrganizationMemberEntity entity = new OrganizationMemberEntity();
        entity.setId(member.id());
        entity.setOrganizationId(member.organizationId().value());
        entity.setUserId(member.userId().value());
        entity.setRole(member.role());
        entity.setCreatedAt(member.createdAt());
        entity.setVersion(member.version());
        return entity;
    }
}
