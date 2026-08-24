package com.mavora.research.api;

import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.research.application.ResearchQueryService;
import com.mavora.research.domain.Competitor;
import com.mavora.research.domain.IcpProfile;
import com.mavora.research.domain.Persona;
import com.mavora.shared.domain.OrganizationId;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/organizations/{organizationId}/research", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Research")
public class ResearchController {

    private final ResearchQueryService researchQueryService;

    public ResearchController(ResearchQueryService researchQueryService) {
        this.researchQueryService = researchQueryService;
    }

    @GetMapping
    public ResearchResponse get(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        var view = researchQueryService.get(new OrganizationId(organizationId), principal.userId());
        return new ResearchResponse(
                view.personas().stream().map(PersonaResponse::from).toList(),
                view.competitors().stream().map(CompetitorResponse::from).toList(),
                view.icp().map(IcpResponse::from).orElse(null)
        );
    }

    public record ResearchResponse(List<PersonaResponse> personas, List<CompetitorResponse> competitors, IcpResponse icp) {
    }

    public record PersonaResponse(UUID id, String name, String summary, String pains, String jobs) {
        static PersonaResponse from(Persona persona) {
            return new PersonaResponse(persona.id(), persona.name(), persona.summary(), persona.pains(), persona.jobs());
        }
    }

    public record CompetitorResponse(UUID id, String name, String url, String notes) {
        static CompetitorResponse from(Competitor competitor) {
            return new CompetitorResponse(competitor.id(), competitor.name(), competitor.url(), competitor.notes());
        }
    }

    public record IcpResponse(UUID id, String summary, String segmentsJson) {
        static IcpResponse from(IcpProfile icp) {
            return new IcpResponse(icp.id(), icp.summary(), icp.segmentsJson());
        }
    }
}
