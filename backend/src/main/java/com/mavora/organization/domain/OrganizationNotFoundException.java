package com.mavora.organization.domain;

import com.mavora.shared.domain.DomainException;

public final class OrganizationNotFoundException extends DomainException {

    public OrganizationNotFoundException() {
        super(ErrorType.NOT_FOUND, "Organization not found", "Organization not found");
    }
}
