package com.mavora.instagram.application;

import java.util.List;

public record InstagramProfileSnapshot(
        String username,
        String name,
        String biography,
        String website,
        Integer followers,
        Integer mediaCount,
        String pageName,
        String pageCategory,
        String pageAbout,
        List<String> recentCaptions
) {
    public String displayName() {
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        if (pageName != null && !pageName.isBlank()) {
            return pageName.trim();
        }
        if (username == null || username.isBlank()) {
            return "Marca";
        }
        String pretty = username.replace('.', ' ').replace('_', ' ').trim();
        if (pretty.length() < 2) {
            return username;
        }
        StringBuilder builder = new StringBuilder();
        for (String part : pretty.split("\\s+")) {
            if (part.isBlank()) {
                continue;
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
            builder.append(' ');
        }
        return builder.toString().trim();
    }
}
