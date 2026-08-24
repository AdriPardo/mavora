package com.mavora.approvals.api;

import com.mavora.approvals.application.ApprovalService;
import com.mavora.approvals.domain.ApprovalRequest;
import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.shared.domain.OrganizationId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/organizations/{organizationId}/approvals", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public ApprovalListResponse list(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return new ApprovalListResponse(approvalService.list(
                new OrganizationId(organizationId), principal.userId()
        ).stream().map(Item::from).toList());
    }

    @PostMapping(value = "/{approvalId}/decide", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Item decide(
            @PathVariable UUID organizationId,
            @PathVariable UUID approvalId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @Valid @RequestBody DecideRequest request
    ) {
        return Item.from(approvalService.decide(
                new OrganizationId(organizationId),
                principal.userId(),
                approvalId,
                request.approved(),
                request.note()
        ));
    }

    public record DecideRequest(boolean approved, @Size(max = 1000) String note) {
    }

    public record ApprovalListResponse(List<Item> items) {
    }

    public record Item(
            UUID id,
            String type,
            String subjectType,
            UUID subjectId,
            String status,
            String summary,
            Instant createdAt,
            Instant decidedAt,
            String decisionNote
    ) {
        static Item from(ApprovalRequest request) {
            return new Item(
                    request.id(),
                    request.type().name(),
                    request.subjectType(),
                    request.subjectId(),
                    request.status().name(),
                    request.summary(),
                    request.createdAt(),
                    request.decidedAt(),
                    request.decisionNote()
            );
        }
    }
}
