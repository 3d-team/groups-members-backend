package com.team3d.awad.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

public class AuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

    private static final Logger LOGGER = LogManager.getLogger(AuthenticationSuccessHandler.class);

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange,
                                              AuthenticationException exception) {
        return Mono.create(sink -> {
            LOGGER.info("Hit {}", AuthenticationFailureHandler.class);
            sink.success();
        });
    }
}
