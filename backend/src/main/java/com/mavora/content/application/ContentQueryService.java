package com.mavora.content.application;

import com.mavora.content.domain.ContentIdea;
import com.mavora.content.domain.ContentIdeaRepository;
import com.mavora.content.domain.ContentPiece;
import com.mavora.content.domain.ContentPieceRepository;
import com.mavora.content.domain.ContentVariant;
import com.mavora.content.domain.ContentVariantRepository;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentQueryService {

    private final OrganizationAuthorizationService authorizationService;
    private final ContentIdeaRepository ideaRepository;
    private final ContentPieceRepository pieceRepository;
    private final ContentVariantRepository variantRepository;

    public ContentQueryService(
            OrganizationAuthorizationService authorizationService,
            ContentIdeaRepository ideaRepository,
            ContentPieceRepository pieceRepository,
            ContentVariantRepository variantRepository
    ) {
        this.authorizationService = authorizationService;
        this.ideaRepository = ideaRepository;
        this.pieceRepository = pieceRepository;
        this.variantRepository = variantRepository;
    }

    @Transactional(readOnly = true)
    public ContentView get(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        List<ContentPiece> pieces = pieceRepository.findByOrganization(organizationId);
        List<PieceView> pieceViews = pieces.stream()
                .map(piece -> new PieceView(piece, variantRepository.findByPiece(piece.id())))
                .toList();
        return new ContentView(ideaRepository.findByOrganization(organizationId), pieceViews);
    }

    public record ContentView(List<ContentIdea> ideas, List<PieceView> pieces) {
    }

    public record PieceView(ContentPiece piece, List<ContentVariant> variants) {
    }
}
