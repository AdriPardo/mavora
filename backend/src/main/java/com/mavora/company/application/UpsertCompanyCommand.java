package com.mavora.company.application;

import java.util.List;

public record UpsertCompanyCommand(
        String name,
        String websiteUrl,
        String description,
        String market,
        List<ProductInput> products
) {
    public record ProductInput(String name, String description, String url) {
    }
}
