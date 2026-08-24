package com.mavora.knowledge.api;

import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.knowledge.application.KnowledgeQueryService;
import com.mavora.knowledge.domain.KnowledgeItem;
import com.mavora.shared.domain.OrganizationId;
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
@RequestMapping(path = "/api/v1/organizations/{organizationId}/knowledge", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Knowledge")
public class KnowledgeController {

    private final KnowledgeQueryService knowledgeQueryService;

    public KnowledgeController(KnowledgeQueryService knowledgeQueryService) {
        this.knowledgeQueryService = knowledgeQueryService;
    }

    @GetMapping
    public KnowledgeListResponse list(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        List<Item> items = knowledgeQueryService.list(new OrganizationId(organizationId), principal.userId())
                .stream()
                .map(Item::from)
                .toList();
        return new KnowledgeListResponse(items);
    }

    public record KnowledgeListResponse(List<Item> items) {
    }

    public record Item(
            UUID id,
            String kind,
            String title,
            String body,
            String source,
            Integer confidence,
            Instant createdAt
    ) {
        static Item from(KnowledgeItem item) {
            return new Item(
                    item.id(),
                    item.kind().name(),
                    item.title(),
                    item.body(),
                    item.source(),
                    item.confidence(),
                    item.createdAt()
            );
        }
    }
}
