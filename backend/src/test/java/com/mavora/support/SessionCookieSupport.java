package com.mavora.support;

import org.springframework.http.HttpHeaders;

public final class SessionCookieSupport {

    private SessionCookieSupport() {
    }

    public static String rawToken(HttpHeaders headers) {
        String setCookie = headers.getFirst(HttpHeaders.SET_COOKIE);
        if (setCookie == null) {
            throw new AssertionError("Missing Set-Cookie");
        }
        String prefix = "mavora_session=";
        int start = setCookie.indexOf(prefix);
        if (start < 0) {
            throw new AssertionError("Missing mavora_session cookie: " + setCookie);
        }
        int from = start + prefix.length();
        int to = setCookie.indexOf(';', from);
        return to < 0 ? setCookie.substring(from) : setCookie.substring(from, to);
    }

    public static String cookieHeader(HttpHeaders headers) {
        return "mavora_session=" + rawToken(headers);
    }
}
