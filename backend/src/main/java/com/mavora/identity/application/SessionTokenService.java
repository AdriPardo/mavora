package com.mavora.identity.application;

public interface SessionTokenService {

    IssuedSessionToken issue();

    String hash(String rawToken);
}
