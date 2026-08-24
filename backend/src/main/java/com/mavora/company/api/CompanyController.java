package com.mavora.company.api;

import com.mavora.company.application.CompanyProfileService;
import com.mavora.company.application.UpsertCompanyCommand;
import com.mavora.company.domain.Company;
import com.mavora.company.domain.MarketingGoal;
import com.mavora.company.domain.Product;
import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.knowledge.domain.KnowledgeItem;
import com.mavora.shared.domain.Money;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/organizations/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Company")
public class CompanyController {

    private final CompanyProfileService companyProfileService;

    public CompanyController(CompanyProfileService companyProfileService) {
        this.companyProfileService = companyProfileService;
    }

    @GetMapping("/company")
    @Operation(summary = "Get the company profile for this organization")
    public CompanyResponse get(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return companyProfileService.get(new OrganizationId(organizationId), principal.userId())
                .map(CompanyController::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    @PutMapping(value = "/company", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create or replace the company profile and products")
    public CompanyResponse upsert(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @Valid @RequestBody UpsertCompanyRequest request
    ) {
        return toResponse(companyProfileService.upsert(
                new OrganizationId(organizationId),
                principal.userId(),
                new UpsertCompanyCommand(
                        request.name(),
                        request.websiteUrl(),
                        request.description(),
                        request.market(),
                        request.products() == null ? List.of() : request.products().stream()
                                .map(product -> new UpsertCompanyCommand.ProductInput(
                                        product.name(), product.description(), product.url()
                                ))
                                .toList()
                )
        ));
    }

    @PostMapping("/company/website-ingest")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Fetch the company website and store a FACT in knowledge")
    public KnowledgeItemResponse ingestWebsite(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        KnowledgeItem item = companyProfileService.ingestWebsite(
                new OrganizationId(organizationId), principal.userId()
        );
        return KnowledgeItemResponse.from(item);
    }

    @GetMapping("/goals")
    public GoalsResponse goals(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        List<GoalResponse> items = companyProfileService.listGoals(
                        new OrganizationId(organizationId), principal.userId()
                ).stream()
                .map(GoalResponse::from)
                .toList();
        return new GoalsResponse(items);
    }

    @GetMapping("/goals/active")
    public GoalResponse activeGoal(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return companyProfileService.activeGoal(new OrganizationId(organizationId), principal.userId())
                .map(GoalResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
    }

    @PostMapping(value = "/goals", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse createGoal(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @Valid @RequestBody CreateGoalRequest request
    ) {
        MarketingGoal goal = companyProfileService.createGoal(
                new OrganizationId(organizationId),
                principal.userId(),
                request.metric(),
                request.targetValue(),
                request.deadline(),
                Money.ofCents(request.budgetCents(), request.budgetCurrency() == null ? "EUR" : request.budgetCurrency()),
                request.market()
        );
        return GoalResponse.from(goal);
    }

    private static CompanyResponse toResponse(CompanyProfileService.CompanyProfile profile) {
        Company company = profile.company();
        return new CompanyResponse(
                company.id(),
                company.name(),
                company.websiteUrl(),
                company.description(),
                company.market(),
                profile.products().stream().map(ProductResponse::from).toList(),
                company.updatedAt()
        );
    }

    public record UpsertCompanyRequest(
            @NotBlank @Size(min = 2, max = 120) String name,
            @Size(max = 2048) String websiteUrl,
            @Size(max = 4000) String description,
            @Size(max = 200) String market,
            List<ProductRequest> products
    ) {
    }

    public record ProductRequest(
            @NotBlank @Size(min = 2, max = 120) String name,
            @Size(max = 2000) String description,
            @Size(max = 2048) String url
    ) {
    }

    public record CreateGoalRequest(
            @NotBlank @Size(min = 2, max = 80) String metric,
            @Positive long targetValue,
            @NotNull LocalDate deadline,
            @Positive long budgetCents,
            @Size(min = 3, max = 3) String budgetCurrency,
            @NotBlank @Size(min = 2, max = 200) String market
    ) {
    }

    public record CompanyResponse(
            UUID id,
            String name,
            String websiteUrl,
            String description,
            String market,
            List<ProductResponse> products,
            Instant updatedAt
    ) {
    }

    public record ProductResponse(UUID id, String name, String description, String url) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(product.id(), product.name(), product.description(), product.url());
        }
    }

    public record GoalsResponse(List<GoalResponse> items) {
    }

    public record GoalResponse(
            UUID id,
            String metric,
            long targetValue,
            LocalDate deadline,
            long budgetCents,
            String budgetCurrency,
            String market,
            String status,
            Instant createdAt
    ) {
        public static GoalResponse from(MarketingGoal goal) {
            return new GoalResponse(
                    goal.id(),
                    goal.metric(),
                    goal.targetValue(),
                    goal.deadline(),
                    goal.budget().amountCents(),
                    goal.budget().currency().getCurrencyCode(),
                    goal.market(),
                    goal.status().name(),
                    goal.createdAt()
            );
        }
    }

    public record KnowledgeItemResponse(
            UUID id,
            String kind,
            String title,
            String body,
            String source,
            Integer confidence,
            Instant createdAt
    ) {
        static KnowledgeItemResponse from(KnowledgeItem item) {
            return new KnowledgeItemResponse(
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
