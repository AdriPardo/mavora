package com.mavora.social.api;

import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.social.application.PublicationService;
import com.mavora.social.domain.Publication;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/organizations/{organizationId}/publications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Social")
public class PublicationController {

    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @GetMapping
    public PublicationListResponse list(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return new PublicationListResponse(publicationService.list(
                new OrganizationId(organizationId), principal.userId()
        ).stream().map(Item::from).toList());
    }

    @PostMapping("/{publicationId}/publish")
    public Item publish(
            @PathVariable UUID organizationId,
            @PathVariable UUID publicationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return Item.from(publicationService.publish(
                new OrganizationId(organizationId), principal.userId(), publicationId
        ));
    }

    public record PublicationListResponse(List<Item> items) {
    }

    public record Item(
            UUID id, UUID pieceId, String channel, String copy, String status, Instant publishedAt, Instant createdAt
    ) {
        static Item from(Publication publication) {
            return new Item(
                    publication.id(),
                    publication.pieceId(),
                    publication.channel(),
                    publication.copy(),
                    publication.status().name(),
                    publication.publishedAt(),
                    publication.createdAt()
            );
        }
    }
}
