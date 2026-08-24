package com.mavora.strategy.application;

import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import com.mavora.strategy.domain.Campaign;
import com.mavora.strategy.domain.CampaignRepository;
import com.mavora.strategy.domain.MarketingStrategy;
import com.mavora.strategy.domain.MarketingStrategyRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StrategyQueryService {

    private final OrganizationAuthorizationService authorizationService;
    private final MarketingStrategyRepository strategyRepository;
    private final CampaignRepository campaignRepository;

    public StrategyQueryService(
            OrganizationAuthorizationService authorizationService,
            MarketingStrategyRepository strategyRepository,
            CampaignRepository campaignRepository
    ) {
        this.authorizationService = authorizationService;
        this.strategyRepository = strategyRepository;
        this.campaignRepository = campaignRepository;
    }

    @Transactional(readOnly = true)
    public Optional<MarketingStrategy> latest(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return strategyRepository.findLatest(organizationId);
    }

    @Transactional(readOnly = true)
    public List<Campaign> campaigns(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return campaignRepository.findByOrganization(organizationId);
    }
}
