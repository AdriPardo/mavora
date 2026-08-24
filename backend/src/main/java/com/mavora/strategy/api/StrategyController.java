package com.mavora.strategy.api;

import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.ResourceNotFoundException;
import com.mavora.strategy.application.StrategyQueryService;
import com.mavora.strategy.domain.Campaign;
import com.mavora.strategy.domain.MarketingStrategy;
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
@RequestMapping(path = "/api/v1/organizations/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Strategy")
public class StrategyController {

    private final StrategyQueryService strategyQueryService;

    public StrategyController(StrategyQueryService strategyQueryService) {
        this.strategyQueryService = strategyQueryService;
    }

    @GetMapping("/strategy")
    public StrategyResponse get(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return strategyQueryService.latest(new OrganizationId(organizationId), principal.userId())
                .map(StrategyResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found"));
    }

    @GetMapping("/campaigns")
    public CampaignListResponse campaigns(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return new CampaignListResponse(strategyQueryService.campaigns(
                new OrganizationId(organizationId), principal.userId()
        ).stream().map(CampaignResponse::from).toList());
    }

    public record StrategyResponse(
            UUID id,
            String status,
            String positioning,
            String icpSummary,
            String channelsJson,
            String pillarsJson,
            String kpisJson,
            String narrative,
            Instant createdAt
    ) {
        public static StrategyResponse from(MarketingStrategy strategy) {
            return new StrategyResponse(
                    strategy.id(),
                    strategy.status().name(),
                    strategy.positioning(),
                    strategy.icpSummary(),
                    strategy.channelsJson(),
                    strategy.pillarsJson(),
                    strategy.kpisJson(),
                    strategy.narrative(),
                    strategy.createdAt()
            );
        }
    }

    public record CampaignListResponse(List<CampaignResponse> items) {
    }

    public record CampaignResponse(UUID id, String name, String status, String channelsJson, Instant createdAt) {
        static CampaignResponse from(Campaign campaign) {
            return new CampaignResponse(
                    campaign.id(), campaign.name(), campaign.status().name(), campaign.channelsJson(), campaign.createdAt()
            );
        }
    }
}
