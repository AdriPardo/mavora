package com.mavora.identity.domain;

import com.mavora.shared.domain.DomainException;

public final class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super(ErrorType.UNAUTHENTICATED, "Invalid credentials", "Invalid email or password");
    }
}
