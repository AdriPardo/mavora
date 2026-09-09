package com.mavora.identity.application;

public record RegisterAccountCommand(
        String email,
        String password,
        String organizationName,
        String ip,
        String userAgent
) {
}
