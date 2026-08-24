package com.mavora.identity.api;

import com.mavora.identity.application.AuthenticationResult;
import com.mavora.identity.application.CurrentAccount;
import com.mavora.identity.application.GetCurrentAccountService;
import com.mavora.identity.application.LoginCommand;
import com.mavora.identity.application.LoginService;
import com.mavora.identity.application.LogoutService;
import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.identity.application.RegisterAccountCommand;
import com.mavora.identity.application.RegisterAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auth")
public class AuthController {

    private final RegisterAccountService registerAccountService;
    private final LoginService loginService;
    private final LogoutService logoutService;
    private final GetCurrentAccountService getCurrentAccountService;
    private final AuthCookieFactory cookies;
    private final Duration sessionTtl;

    public AuthController(
            RegisterAccountService registerAccountService,
            LoginService loginService,
            LogoutService logoutService,
            GetCurrentAccountService getCurrentAccountService,
            AuthCookieFactory cookies,
            @Value("${mavora.auth.session-ttl}") Duration sessionTtl
    ) {
        this.registerAccountService = registerAccountService;
        this.loginService = loginService;
        this.logoutService = logoutService;
        this.getCurrentAccountService = getCurrentAccountService;
        this.cookies = cookies;
        this.sessionTtl = sessionTtl;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a user and create their organization")
    public ResponseEntity<CurrentAccountResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticationResult result = registerAccountService.register(new RegisterAccountCommand(
                request.email(),
                request.password(),
                request.organizationName(),
                clientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        ));
        return withSession(result);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Log in")
    public ResponseEntity<CurrentAccountResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticationResult result = loginService.login(new LoginCommand(
                request.email(),
                request.password(),
                clientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        ));
        return withSession(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out and revoke the current session")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        logoutService.logout(readCookie(httpRequest), clientIp(httpRequest));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Current user and organizations")
    public CurrentAccountResponse me(@AuthenticationPrincipal MavoraPrincipal principal) {
        return CurrentAccountResponse.from(getCurrentAccountService.get(principal.userId()));
    }

    private ResponseEntity<CurrentAccountResponse> withSession(AuthenticationResult result) {
        CurrentAccount account = getCurrentAccountService.get(result.userId());
        Duration maxAge = Duration.between(Instant.now(), result.expiresAt());
        if (maxAge.isNegative() || maxAge.isZero()) {
            maxAge = sessionTtl;
        }
        return cookies.withSession(CurrentAccountResponse.from(account), result.rawSessionToken(), maxAge);
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookiesOnRequest = request.getCookies();
        if (cookiesOnRequest == null) {
            return null;
        }
        for (Cookie cookie : cookiesOnRequest) {
            if (cookies.cookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
