package com.mavora.content.api;

import com.mavora.content.application.ContentQueryService;
import com.mavora.content.domain.ContentIdea;
import com.mavora.content.domain.ContentPiece;
import com.mavora.content.domain.ContentVariant;
import com.mavora.identity.application.MavoraPrincipal;
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
@RequestMapping(path = "/api/v1/organizations/{organizationId}/content", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Content")
public class ContentController {

    private final ContentQueryService contentQueryService;

    public ContentController(ContentQueryService contentQueryService) {
        this.contentQueryService = contentQueryService;
    }

    @GetMapping
    public ContentResponse get(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        var view = contentQueryService.get(new OrganizationId(organizationId), principal.userId());
        return new ContentResponse(
                view.ideas().stream().map(IdeaResponse::from).toList(),
                view.pieces().stream().map(PieceResponse::from).toList()
        );
    }

    public record ContentResponse(List<IdeaResponse> ideas, List<PieceResponse> pieces) {
    }

    public record IdeaResponse(UUID id, String title, String angle, String pillar, String status) {
        static IdeaResponse from(ContentIdea idea) {
            return new IdeaResponse(idea.id(), idea.title(), idea.angle(), idea.pillar(), idea.status());
        }
    }

    public record PieceResponse(
            UUID id, String title, String body, String status, List<VariantResponse> variants
    ) {
        static PieceResponse from(ContentQueryService.PieceView view) {
            ContentPiece piece = view.piece();
            return new PieceResponse(
                    piece.id(),
                    piece.title(),
                    piece.body(),
                    piece.status().name(),
                    view.variants().stream().map(VariantResponse::from).toList()
            );
        }
    }

    public record VariantResponse(UUID id, String channel, String body) {
        static VariantResponse from(ContentVariant variant) {
            return new VariantResponse(variant.id(), variant.channel(), variant.body());
        }
    }
}
