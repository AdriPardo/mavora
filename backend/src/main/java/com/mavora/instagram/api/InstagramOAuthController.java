package com.mavora.instagram.api;

import com.mavora.instagram.application.InstagramAccountService;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstagramOAuthController {

    private final InstagramAccountService accountService;
    private final String appUrl;

    public InstagramOAuthController(
            InstagramAccountService accountService,
            @Value("${mavora.public.app-url:http://localhost:5173}") String appUrl
    ) {
        this.accountService = accountService;
        this.appUrl = appUrl.endsWith("/") ? appUrl.substring(0, appUrl.length() - 1) : appUrl;
    }

    @GetMapping("/api/v1/integrations/instagram/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error
    ) {
        String target;
        try {
            if (error != null && !error.isBlank()) {
                target = appUrl + "/integrations?instagram=error";
            } else {
                accountService.completeOAuth(code, state);
                target = appUrl + "/integrations?instagram=connected";
            }
        } catch (RuntimeException exception) {
            target = appUrl + "/integrations?instagram=error";
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }
}
