package com.mavora.shared.domain;

public class DomainException extends RuntimeException {

    private final ErrorType type;
    private final String title;

    public DomainException(ErrorType type, String title, String message) {
        super(message);
        this.type = type;
        this.title = title;
    }

    public DomainException(String message) {
        this(ErrorType.RULE, "Domain rule violated", message);
    }

    public ErrorType type() {
        return type;
    }

    public String title() {
        return title;
    }

    public enum ErrorType {
        RULE,
        CONFLICT,
        UNAUTHENTICATED,
        NOT_FOUND,
        FORBIDDEN,
        RATE_LIMITED
    }
}
