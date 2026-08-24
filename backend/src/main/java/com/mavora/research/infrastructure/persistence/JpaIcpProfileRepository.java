package com.mavora.research.infrastructure.persistence;
import com.mavora.research.domain.IcpProfile;
import com.mavora.research.domain.IcpProfileRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaIcpProfileRepository implements IcpProfileRepository {
    private final IcpProfileJpaRepository jpa;
    public JpaIcpProfileRepository(IcpProfileJpaRepository jpa) { this.jpa = jpa; }
    @Override public IcpProfile save(IcpProfile profile) {
        IcpProfileEntity e = toEntity(profile); e.markNew(!jpa.existsById(profile.id()));
        return toDomain(jpa.save(e));
    }
    @Override public Optional<IcpProfile> findLatest(OrganizationId organizationId) {
        return jpa.findFirstByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).map(this::toDomain);
    }
    private IcpProfile toDomain(IcpProfileEntity e) {
        return IcpProfile.reconstitute(e.getId(), new OrganizationId(e.getOrganizationId()), e.getStrategyId(), e.getSummary(), e.getSegmentsJson(), e.getCreatedAt(), e.getVersion());
    }
    private IcpProfileEntity toEntity(IcpProfile d) {
        IcpProfileEntity e = new IcpProfileEntity();
        e.setId(d.id()); e.setOrganizationId(d.organizationId().value()); e.setStrategyId(d.strategyId());
        e.setSummary(d.summary()); e.setSegmentsJson(d.segmentsJson()); e.setCreatedAt(d.createdAt()); e.setVersion(d.version());
        return e;
    }
}
