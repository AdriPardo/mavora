package com.mavora.identity.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern SIMPLE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        Objects.requireNonNull(value, "email is required");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 320) {
            throw new IllegalArgumentException("email is too long");
        }
        if (!SIMPLE.matcher(value).matches()) {
            throw new IllegalArgumentException("email is invalid");
        }
    }

    public String normalized() {
        return value;
    }
}
