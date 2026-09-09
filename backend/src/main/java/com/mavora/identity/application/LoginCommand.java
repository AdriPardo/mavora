package com.mavora.identity.application;

public record LoginCommand(String email, String password, String ip, String userAgent) {
}
