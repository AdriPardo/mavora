package com.mavora.research.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;

public interface PersonaRepository {

    Persona save(Persona persona);
    List<Persona> findByOrganization(OrganizationId organizationId);

}
