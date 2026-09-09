package com.mavora.instagram.api;

import com.mavora.agents.api.WorkflowController;
import com.mavora.agents.application.EnqueueWorkflowService;
import com.mavora.agents.domain.WorkflowType;
import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.instagram.application.BrandBriefService;
import com.mavora.instagram.application.InstagramAccountService;
import com.mavora.instagram.application.InstagramQueryService;
import com.mavora.instagram.application.MetaConnectionService;
import com.mavora.instagram.application.MediaLibraryService;
import com.mavora.instagram.domain.BrandBrief;
import com.mavora.instagram.domain.InstagramSlot;
import com.mavora.shared.domain.OrganizationId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/organizations/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Instagram")
public class InstagramController {

    private final InstagramAccountService accountService;
    private final MetaConnectionService metaConnectionService;
    private final BrandBriefService briefService;
    private final MediaLibraryService mediaLibraryService;
    private final InstagramQueryService queryService;
    private final EnqueueWorkflowService enqueueWorkflowService;

    public InstagramController(
            InstagramAccountService accountService,
            MetaConnectionService metaConnectionService,
            BrandBriefService briefService,
            MediaLibraryService mediaLibraryService,
            InstagramQueryService queryService,
            EnqueueWorkflowService enqueueWorkflowService
    ) {
        this.accountService = accountService;
        this.metaConnectionService = metaConnectionService;
        this.briefService = briefService;
        this.mediaLibraryService = mediaLibraryService;
        this.queryService = queryService;
        this.enqueueWorkflowService = enqueueWorkflowService;
    }

    @GetMapping("/instagram")
    @Operation(summary = "Instagram connection status")
    public InstagramAccountService.Status status(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return accountService.status(new OrganizationId(organizationId), principal.userId());
    }

    @GetMapping("/instagram/meta")
    @Operation(summary = "Meta app setup for Instagram OAuth")
    public MetaConnectionService.SetupView metaSetup(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return metaConnectionService.setup(new OrganizationId(organizationId), principal.userId());
    }

    @PutMapping(value = "/instagram/meta", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MetaConnectionService.SetupView saveMeta(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @Valid @RequestBody MetaAppRequest request
    ) {
        return metaConnectionService.save(
                new OrganizationId(organizationId),
                principal.userId(),
                request.appId(),
                request.appSecret(),
                request.redirectUri(),
                request.graphVersion()
        );
    }

    @DeleteMapping("/instagram/meta")
    public MetaConnectionService.SetupView clearMeta(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return metaConnectionService.clear(new OrganizationId(organizationId), principal.userId());
    }

    @GetMapping("/instagram/connect-url")
    public InstagramAccountService.ConnectUrl connectUrl(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return accountService.connectUrl(new OrganizationId(organizationId), principal.userId());
    }

    @PostMapping(value = "/instagram/connect-token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public InstagramAccountService.Status connectToken(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @Valid @RequestBody ConnectTokenRequest request
    ) {
        return accountService.connectWithToken(
                new OrganizationId(organizationId),
                principal.userId(),
                request.username(),
                request.igUserId(),
                request.pageId(),
                request.accessToken()
        );
    }

    @PostMapping(value = "/instagram/connect-fake", consumes = MediaType.APPLICATION_JSON_VALUE)
    public InstagramAccountService.Status connectFake(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @Valid @RequestBody ConnectFakeRequest request
    ) {
        return accountService.connectFake(
                new OrganizationId(organizationId), principal.userId(), request.username()
        );
    }

    @PatchMapping(value = "/instagram/autonomy", consumes = MediaType.APPLICATION_JSON_VALUE)
    public InstagramAccountService.Status autonomy(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @RequestBody AutonomyRequest request
    ) {
        return accountService.setAutonomy(
                new OrganizationId(organizationId), principal.userId(), request.autonomyEnabled()
        );
    }

    @PostMapping("/instagram/disconnect")
    public InstagramAccountService.Status disconnect(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return accountService.disconnect(new OrganizationId(organizationId), principal.userId());
    }

    @GetMapping("/instagram/brief")
    public BriefResponse brief(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return briefService.get(new OrganizationId(organizationId), principal.userId())
                .map(BriefResponse::from)
                .orElse(BriefResponse.empty());
    }

    @PutMapping(value = "/instagram/brief", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BriefResponse upsertBrief(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @Valid @RequestBody BriefRequest request
    ) {
        return BriefResponse.from(briefService.upsert(
                new OrganizationId(organizationId),
                principal.userId(),
                request.voice(),
                request.offer(),
                request.cta(),
                request.audience(),
                request.extraNotes()
        ));
    }

    @GetMapping("/media")
    public MediaListResponse media(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return new MediaListResponse(mediaLibraryService.list(new OrganizationId(organizationId), principal.userId()));
    }

    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaLibraryService.MediaView upload(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "captionHint", required = false) String captionHint,
            @RequestParam(value = "productId", required = false) UUID productId
    ) throws Exception {
        return mediaLibraryService.upload(
                new OrganizationId(organizationId),
                principal.userId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                captionHint,
                productId
        );
    }

    @DeleteMapping("/media/{assetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedia(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @PathVariable UUID assetId
    ) {
        mediaLibraryService.delete(new OrganizationId(organizationId), principal.userId(), assetId);
    }

    @GetMapping("/instagram/slots")
    public SlotListResponse slots(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return new SlotListResponse(queryService.slots(new OrganizationId(organizationId), principal.userId())
                .stream()
                .map(SlotResponse::from)
                .toList());
    }

    @GetMapping("/instagram/playbook")
    public InstagramQueryService.Playbook playbook(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return queryService.playbook(new OrganizationId(organizationId), principal.userId());
    }

    @PostMapping("/instagram/week")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public WorkflowController.WorkflowResponse generateWeek(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return WorkflowController.WorkflowResponse.from(enqueueWorkflowService.enqueue(
                new OrganizationId(organizationId),
                principal.userId(),
                WorkflowType.INSTAGRAM_WEEK,
                "{}"
        ));
    }

    public record MetaAppRequest(
            @NotBlank @Size(min = 5, max = 64) String appId,
            @Size(max = 256) String appSecret,
            @Size(max = 500) String redirectUri,
            @Size(max = 16) String graphVersion
    ) {
    }

    public record ConnectTokenRequest(
            @NotBlank @Size(min = 1, max = 30) String username,
            @NotBlank @Size(min = 1, max = 64) String igUserId,
            @Size(max = 64) String pageId,
            @NotBlank @Size(min = 20, max = 4000) String accessToken
    ) {
    }

    public record ConnectFakeRequest(@NotBlank @Size(min = 1, max = 30) String username) {
    }

    public record AutonomyRequest(boolean autonomyEnabled) {
    }

    public record BriefRequest(
            @Size(max = 500) String voice,
            @Size(max = 2000) String offer,
            @Size(max = 300) String cta,
            @Size(max = 500) String audience,
            @Size(max = 4000) String extraNotes
    ) {
    }

    public record BriefResponse(
            String voice,
            String offer,
            String cta,
            String audience,
            String extraNotes,
            Instant updatedAt
    ) {
        static BriefResponse empty() {
            return new BriefResponse(null, null, null, null, null, null);
        }

        static BriefResponse from(BrandBrief brief) {
            return new BriefResponse(
                    brief.voice(), brief.offer(), brief.cta(), brief.audience(), brief.extraNotes(), brief.updatedAt()
            );
        }
    }

    public record MediaListResponse(List<MediaLibraryService.MediaView> items) {
    }

    public record SlotListResponse(List<SlotResponse> items) {
    }

    public record SlotResponse(
            UUID id,
            String format,
            String status,
            Instant scheduledAt,
            String hook,
            String caption,
            String cta,
            List<String> hashtags,
            List<UUID> mediaAssetIds,
            String igMediaId,
            String errorMessage,
            Instant publishedAt
    ) {
        static SlotResponse from(InstagramSlot slot) {
            return new SlotResponse(
                    slot.id(),
                    slot.format().name(),
                    slot.status().name(),
                    slot.scheduledAt(),
                    slot.hook(),
                    slot.caption(),
                    slot.cta(),
                    slot.hashtags(),
                    slot.mediaAssetIds(),
                    slot.igMediaId(),
                    slot.errorMessage(),
                    slot.publishedAt()
            );
        }
    }
}
