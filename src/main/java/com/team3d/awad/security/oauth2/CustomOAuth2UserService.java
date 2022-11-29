package com.team3d.awad.security.oauth2;

import com.team3d.awad.entity.User;
import com.team3d.awad.repository.UserRepository;
import com.team3d.awad.security.CustomUserDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        return service.loadUser(userRequest).flatMap(oidcUser -> {
            Map<String, Object> attributes = oidcUser.getAttributes();
            LOGGER.info("OAuth2 response: {}", attributes);
            String email = oidcUser.getEmail();
            return userRepository.existsByEmail(email)
                    .flatMap(exist -> {
                        if (exist) {
                            return updateUser(oidcUser);
                        }

                        return registerUser(userRequest, oidcUser);
                    });
        });
    }

    private Mono<OidcUser> registerUser(OidcUserRequest userRequest, OidcUser oidcUser) {
        User user = User.builder()
                .email(oidcUser.getEmail())
                .provider(userRequest.getClientRegistration().getRegistrationId())
                .providerId(oidcUser.getIdToken().toString())
                .build();
        return userRepository.save(user)
                .cast(User.class)
                .flatMap(data -> {
                    OidcUser userDetails = new CustomUserDetails(data.getUuid(), data.getEmail(),
                            data.getPassword(), oidcUser.getAuthorities());
                    return Mono.just(userDetails);
                });
    }

    private Mono<OidcUser> updateUser(OidcUser oidcUser) {
        return userRepository.findByEmail(oidcUser.getEmail())
                .flatMap(data -> {
                    LOGGER.info("Load user from OAuth2");
                    LOGGER.info("User data: studentID: {}, email: {}, fullName: {}",
                            data.getStudentId(), data.getEmail(), data.getFullName());
                    OidcUser userDetails = new CustomUserDetails(data.getUuid(), data.getEmail(),
                            data.getPassword(), oidcUser.getAuthorities());
                    return Mono.just(userDetails);
                });
    }
}
