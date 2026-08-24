package com.mavora.identity.application;

public record IssuedSessionToken(String rawToken, String tokenHash) {
}
