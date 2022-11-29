package com.team3d.awad.security.oauth2;

import com.team3d.awad.entity.User;
import com.team3d.awad.repository.UserRepository;
import com.team3d.awad.security.CustomUserDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import reactor.core.publisher.Mono;

import java.util.Map;

public class CustomOAuth2UserService implements ReactiveOAuth2UserService<OidcUserRequest, OidcUser> {

    private static final Logger LOGGER = LogManager.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    private final OidcReactiveOAuth2UserService service = new OidcReactiveOAuth2UserService();

    @Override
    public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return service.loadUser(userRequest).map(oidcUser -> {
            Map<String, Object> attributes = oidcUser.getAttributes();
            LOGGER.info("OAuth2 response: {}", attributes);

            LOGGER.info("Authorities: {}", oidcUser.getAuthorities());
            return new CustomUserDetails("6383842a2fd7028f30d433a6", oidcUser.getEmail(),
                    null, oidcUser.getAuthorities());
        });
    }

    private User registerUser(OidcUser oidcUser) {
        return User.builder().build();
    }

    private User updateUser(OidcUser oidcUser) {
        return User.builder().build();
    }
}
