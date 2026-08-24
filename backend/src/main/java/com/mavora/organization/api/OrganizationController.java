package com.mavora.organization.api;

import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.organization.application.OrganizationQueryService;
import com.mavora.shared.domain.OrganizationId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Organizations")
public class OrganizationController {

    private final OrganizationQueryService organizationQueryService;

    public OrganizationController(OrganizationQueryService organizationQueryService) {
        this.organizationQueryService = organizationQueryService;
    }

    @GetMapping("/{organizationId}")
    @Operation(summary = "Get an organization the current user belongs to")
    public OrganizationResponse get(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        var view = organizationQueryService.get(new OrganizationId(organizationId), principal.userId());
        return new OrganizationResponse(
                view.id().value(),
                view.name(),
                view.slug(),
                view.plan(),
                view.status(),
                view.role()
        );
    }

    @GetMapping("/{organizationId}/members")
    @Operation(summary = "List members of an organization")
    public MembersResponse members(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        List<MemberResponse> items = organizationQueryService.listMembers(
                        new OrganizationId(organizationId),
                        principal.userId()
                ).stream()
                .map(member -> new MemberResponse(
                        member.userId().value(),
                        member.email(),
                        member.role(),
                        member.createdAt()
                ))
                .toList();
        return new MembersResponse(items);
    }

    public record OrganizationResponse(
            UUID id,
            String name,
            String slug,
            String plan,
            String status,
            String role
    ) {
    }

    public record MembersResponse(List<MemberResponse> items) {
    }

    public record MemberResponse(UUID userId, String email, String role, Instant createdAt) {
    }
}
