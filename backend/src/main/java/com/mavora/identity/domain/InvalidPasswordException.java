package com.mavora.identity.domain;

import com.mavora.shared.domain.DomainException;

public final class InvalidPasswordException extends DomainException {

    public InvalidPasswordException(String message) {
        super(ErrorType.RULE, "Invalid password", message);
    }
}
