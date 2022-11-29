package com.team3d.awad.utils;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;

public class RequestUtils {

    public static String getJwtFromRequest(ServerHttpRequest request) {
        String bearerToken = String.valueOf(request.getHeaders().getFirst("Authorization"));
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
