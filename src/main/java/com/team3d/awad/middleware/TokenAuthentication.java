package com.team3d.awad.middleware;

import com.team3d.awad.security.CustomUserDetailsService;
import com.team3d.awad.security.TokenProvider;
import com.team3d.awad.utils.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class TokenAuthentication implements WebFilter {

    private static final Logger LOGGER = LogManager.getLogger(TokenAuthentication.class);

    private final TokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    public TokenAuthentication(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String URI = request.getURI().getPath();
        if (URI.contains("login") || URI.contains("register")) {
            return chain.filter(exchange);
        }
        if (URI.contains("socket.io")) {
            return chain.filter(exchange);
        }
        if (URI.contains("ws")) {
            return chain.filter(exchange);
        }

        final String JWT = RequestUtils.getJwtFromRequest(exchange.getRequest());
        if (!StringUtils.hasText(JWT) || !tokenProvider.validateToken(JWT)) {
            LOGGER.info("[!] Not found JWT on request, request ID: {}", exchange.getRequest().getId());
            return Mono.error(new UsernameNotFoundException("User not found"));
        }
        String userId = tokenProvider.getUserIdFromToken(JWT);
        return userDetailsService.findByUserId(userId)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                .map(user -> {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user.getUsername(),
                            user.getPassword(), user.getAuthorities());
                    return ReactiveSecurityContextHolder.withAuthentication(authentication);
                })
                .then(chain.filter(exchange));
    }
}
