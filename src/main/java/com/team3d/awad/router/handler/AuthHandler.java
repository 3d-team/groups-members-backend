package com.team3d.awad.router.handler;

import com.team3d.awad.payload.LoginRequest;
import com.team3d.awad.repository.UserRepository;
import com.team3d.awad.security.TokenProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AuthHandler {

    private static final Logger LOGGER = LogManager.getLogger(AuthHandler.class);

    private final UserRepository userRepository;

    private final TokenProvider tokenProvider;

    public AuthHandler(UserRepository userRepository, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public Mono<ServerResponse> login(ServerRequest request) {

        return request.bodyToMono(LoginRequest.class)
                .flatMap(payload -> userRepository.findByEmail(payload.getEmail())
                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Email not found")))
                        .flatMap(user -> {
                            LOGGER.info("Hit API login with {}", user.getEmail());
                            if (user.getPassword().equals(payload.getPassword())) {
                                String token = tokenProvider.createToken(user.getUuid());
                                return ServerResponse.ok()
                                        .body(Mono.just(token), String.class);
                            }

                            return ServerResponse.notFound().build();
                        }));
    }
}
