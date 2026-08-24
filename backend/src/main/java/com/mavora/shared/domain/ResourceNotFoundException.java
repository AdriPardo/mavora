package com.mavora.shared.domain;

public final class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String title) {
        super(ErrorType.NOT_FOUND, title, title);
    }
}
