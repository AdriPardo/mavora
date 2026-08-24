package com.mavora.shared.domain;

public final class ForbiddenActionException extends DomainException {

    public ForbiddenActionException() {
        super(ErrorType.FORBIDDEN, "Forbidden", "You cannot perform this action");
    }
}
