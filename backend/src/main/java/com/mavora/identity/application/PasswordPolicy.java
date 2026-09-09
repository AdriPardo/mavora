package com.mavora.identity.application;

import com.mavora.identity.domain.InvalidPasswordException;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 72;

    private PasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new InvalidPasswordException("Password must be at least " + MIN_LENGTH + " characters");
        }
        if (rawPassword.length() > MAX_LENGTH) {
            throw new InvalidPasswordException("Password must be at most " + MAX_LENGTH + " characters");
        }
        if (rawPassword.isBlank()) {
            throw new InvalidPasswordException("Password cannot be blank");
        }
    }
}
