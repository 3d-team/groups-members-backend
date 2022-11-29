package com.team3d.awad.security;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private static final Logger LOGGER = LogManager.getLogger(AuthenticationEntryPoint.class);

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpRequest request = exchange.getRequest();
        if (CollectionUtils.isNotEmpty(request.getHeaders().get("Authorization"))) {
            LOGGER.info("Hit {}", AuthenticationEntryPoint.class);
            return Mono.error(ex);
        } else {
            LOGGER.info("Unauthenticated access attempt to resource {} with HttpMethod of {} Token: {}",
                    exchange.getRequest().getPath(),
                    request.getMethod().name(),
                    request.getHeaders().get("Authorization"));
            return Mono.error(new Exception("Please login to access this resource!"));
        }
    }
}
