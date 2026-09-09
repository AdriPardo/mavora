package com.mavora.organization.domain;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class OrganizationSlug {

    private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");
    private static final Pattern TRIM_HYPHEN = Pattern.compile("(^-+)|(-+$)");

    private OrganizationSlug() {
    }

    public static String fromName(String name) {
        if (name == null) {
            return "org";
        }
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        String slug = NON_SLUG.matcher(normalized).replaceAll("-");
        slug = TRIM_HYPHEN.matcher(slug).replaceAll("");
        if (slug.length() > 50) {
            slug = slug.substring(0, 50);
            slug = TRIM_HYPHEN.matcher(slug).replaceAll("");
        }
        return slug.isBlank() ? "org" : slug;
    }
}
