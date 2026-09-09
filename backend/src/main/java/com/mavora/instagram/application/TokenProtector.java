package com.mavora.instagram.application;

public interface TokenProtector {

    String encrypt(String plaintext);

    String decrypt(String ciphertext);

    String sign(String message);

    boolean verify(String message, String signature);
}
