package com.team3d.awad.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;

public class AuthenticationSuccessHandler extends RedirectServerAuthenticationSuccessHandler {

    private static final Logger LOGGER = LogManager.getLogger(AuthenticationSuccessHandler.class);

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        LOGGER.info("Hit {}", AuthenticationSuccessHandler.class);
        return webFilterExchange.getChain().filter(webFilterExchange.getExchange());
    }
}
