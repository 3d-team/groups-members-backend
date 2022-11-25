package com.team3d.awad.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/me")
    Mono<Map<String, Object>> userInfo(@AuthenticationPrincipal OidcUser oidcUser) {
        return Mono.just(oidcUser.getAttributes());
    }
}
