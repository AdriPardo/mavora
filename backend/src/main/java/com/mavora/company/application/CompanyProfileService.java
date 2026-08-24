package com.mavora.company.application;

import com.mavora.company.domain.Company;
import com.mavora.company.domain.CompanyRepository;
import com.mavora.company.domain.MarketingGoal;
import com.mavora.company.domain.MarketingGoalRepository;
import com.mavora.company.domain.Product;
import com.mavora.company.domain.ProductRepository;
import com.mavora.knowledge.domain.KnowledgeItem;
import com.mavora.knowledge.domain.KnowledgeItemRepository;
import com.mavora.knowledge.domain.KnowledgeKind;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.Money;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyProfileService {

    private final OrganizationAuthorizationService authorizationService;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final MarketingGoalRepository goalRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final WebsiteFetcher websiteFetcher;
    private final Clock clock;

    public CompanyProfileService(
            OrganizationAuthorizationService authorizationService,
            CompanyRepository companyRepository,
            ProductRepository productRepository,
            MarketingGoalRepository goalRepository,
            KnowledgeItemRepository knowledgeItemRepository,
            WebsiteFetcher websiteFetcher,
            Clock clock
    ) {
        this.authorizationService = authorizationService;
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.goalRepository = goalRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.websiteFetcher = websiteFetcher;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Optional<CompanyProfile> get(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return companyRepository.findByOrganization(organizationId).map(this::toProfile);
    }

    @Transactional
    public CompanyProfile upsert(OrganizationId organizationId, UserId userId, UpsertCompanyCommand command) {
        authorizationService.requireWriter(organizationId, userId);
        Instant now = clock.instant();
        Company company = companyRepository.findByOrganization(organizationId)
                .map(existing -> {
                    existing.update(
                            command.name(),
                            command.websiteUrl(),
                            command.description(),
                            command.market(),
                            now
                    );
                    return existing;
                })
                .orElseGet(() -> Company.create(
                        organizationId,
                        command.name(),
                        command.websiteUrl(),
                        command.description(),
                        command.market(),
                        now
                ));
        Company saved = companyRepository.save(company);
        productRepository.deleteByCompany(saved.id(), organizationId);
        List<Product> products = new ArrayList<>();
        if (command.products() != null) {
            for (UpsertCompanyCommand.ProductInput input : command.products()) {
                products.add(productRepository.save(Product.create(
                        organizationId,
                        saved.id(),
                        input.name(),
                        input.description(),
                        input.url(),
                        now
                )));
            }
        }
        return new CompanyProfile(saved, List.copyOf(products));
    }

    @Transactional
    public KnowledgeItem ingestWebsite(OrganizationId organizationId, UserId userId) {
        authorizationService.requireWriter(organizationId, userId);
        Company company = companyRepository.findByOrganization(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Create the company before ingesting a website"));
        if (company.websiteUrl() == null) {
            throw new IllegalArgumentException("Company has no website URL");
        }
        FetchedPage page = websiteFetcher.fetch(company.websiteUrl());
        return knowledgeItemRepository.save(KnowledgeItem.create(
                organizationId,
                KnowledgeKind.FACT,
                page.title() == null || page.title().isBlank() ? "Website " + page.url() : page.title(),
                page.text(),
                page.url(),
                80,
                clock.instant()
        ));
    }

    @Transactional
    public MarketingGoal createGoal(
            OrganizationId organizationId,
            UserId userId,
            String metric,
            long targetValue,
            LocalDate deadline,
            Money budget,
            String market
    ) {
        authorizationService.requireWriter(organizationId, userId);
        Instant now = clock.instant();
        goalRepository.findActive(organizationId).ifPresent(active -> {
            active.archive(now);
            goalRepository.save(active);
        });
        return goalRepository.save(MarketingGoal.create(
                organizationId, metric, targetValue, deadline, budget, market, now
        ));
    }

    @Transactional(readOnly = true)
    public Optional<MarketingGoal> activeGoal(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return goalRepository.findActive(organizationId);
    }

    @Transactional(readOnly = true)
    public List<MarketingGoal> listGoals(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return goalRepository.findByOrganization(organizationId);
    }

    private CompanyProfile toProfile(Company company) {
        return new CompanyProfile(company, productRepository.findByCompany(company.id()));
    }

    public record CompanyProfile(Company company, List<Product> products) {
    }
}
