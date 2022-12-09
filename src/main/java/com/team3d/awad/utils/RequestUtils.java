package com.team3d.awad.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;

public class RequestUtils {

    private static final Logger LOGGER = LogManager.getLogger(RequestUtils.class);

    public static String getJwtFromRequest(ServerHttpRequest request) {
        String bearerToken = String.valueOf(request.getHeaders().getFirst("Authorization"));
        LOGGER.info("Bearer token: {}", bearerToken);
        String bearerHeader = "Bearer ";
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(bearerHeader)) {
            return bearerToken.substring(bearerHeader.length());
        }
        return null;
    }

    public static String getJwtFromRequest(ServerRequest request) {
        String bearerToken = String.valueOf(request.headers().firstHeader("Authorization"));
        String bearerHeader = "Bearer ";
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(bearerHeader)) {
            return bearerToken.substring(bearerHeader.length());
        }
        return null;
    }
}
