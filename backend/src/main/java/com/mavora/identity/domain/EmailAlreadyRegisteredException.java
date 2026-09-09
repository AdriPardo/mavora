package com.mavora.identity.domain;

import com.mavora.shared.domain.DomainException;

public final class EmailAlreadyRegisteredException extends DomainException {

    public EmailAlreadyRegisteredException() {
        super(ErrorType.CONFLICT, "Email already registered", "An account with this email already exists");
    }
}
