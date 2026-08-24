package com.mavora.research.infrastructure.persistence;
import com.mavora.research.domain.Persona;
import com.mavora.research.domain.PersonaRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaPersonaRepository implements PersonaRepository {
    private final PersonaJpaRepository jpa;
    public JpaPersonaRepository(PersonaJpaRepository jpa) { this.jpa = jpa; }
    @Override public Persona save(Persona persona) {
        PersonaEntity e = toEntity(persona); e.markNew(!jpa.existsById(persona.id()));
        return toDomain(jpa.save(e));
    }
    @Override public List<Persona> findByOrganization(OrganizationId organizationId) {
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream().map(this::toDomain).toList();
    }
    private Persona toDomain(PersonaEntity e) {
        return Persona.reconstitute(e.getId(), new OrganizationId(e.getOrganizationId()), e.getStrategyId(), e.getName(), e.getSummary(), e.getPains(), e.getJobs(), e.getCreatedAt(), e.getVersion());
    }
    private PersonaEntity toEntity(Persona d) {
        PersonaEntity e = new PersonaEntity();
        e.setId(d.id()); e.setOrganizationId(d.organizationId().value()); e.setStrategyId(d.strategyId());
        e.setName(d.name()); e.setSummary(d.summary()); e.setPains(d.pains()); e.setJobs(d.jobs());
        e.setCreatedAt(d.createdAt()); e.setVersion(d.version());
        return e;
    }
}
