package com.mavora.identity.infrastructure.security;

import com.mavora.identity.application.SessionTokenService;
import com.mavora.identity.domain.SessionRepository;
import com.mavora.identity.domain.UserRepository;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionFilterConfig {

    @Bean
    SessionAuthenticationFilter sessionAuthenticationFilter(
            SessionRepository sessionRepository,
            UserRepository userRepository,
            SessionTokenService sessionTokenService,
            Clock clock,
            @Value("${mavora.auth.cookie-name}") String cookieName
    ) {
        return new SessionAuthenticationFilter(
                sessionRepository,
                userRepository,
                sessionTokenService,
                clock,
                cookieName
        );
    }

    @Bean
    FilterRegistrationBean<SessionAuthenticationFilter> sessionAuthenticationFilterRegistration(
            SessionAuthenticationFilter filter
    ) {
        FilterRegistrationBean<SessionAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
