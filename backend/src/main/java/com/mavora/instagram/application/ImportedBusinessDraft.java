package com.mavora.instagram.application;

public record ImportedBusinessDraft(
        String companyName,
        String websiteUrl,
        String description,
        String market,
        String productName,
        String productDescription,
        String voice,
        String offer,
        String cta,
        String audience,
        String extraNotes
) {
}
