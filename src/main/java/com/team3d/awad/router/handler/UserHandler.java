package com.team3d.awad.router.handler;

import com.team3d.awad.entity.User;
import com.team3d.awad.payload.Email;
import com.team3d.awad.payload.RegisterUserRequest;
import com.team3d.awad.payload.UpdateProfileRequest;
import com.team3d.awad.repository.UserRepository;
import com.team3d.awad.security.TokenProvider;
import com.team3d.awad.service.MailService;
import com.team3d.awad.utils.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class UserHandler {

    private static final Logger LOGGER = LogManager.getLogger(UserHandler.class);
    private final UserRepository userRepository;
    private final MailService mailService;
    private final TokenProvider tokenProvider;

    public UserHandler(UserRepository userRepository, MailService mailService, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.tokenProvider = tokenProvider;
    }

    public Mono<ServerResponse> all(ServerRequest request) {
        return ServerResponse.ok().build();
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(RegisterUserRequest.class)
                .flatMap(payload -> {
                    User user = User.builder()
                            .fullName(payload.getFullName())
                            .email(payload.getEmail())
                            .password(payload.getPassword())
                            .studentId(payload.getStudentId())
                            .build();
                    return userRepository.save(user);
                })
                .flatMap(user -> {
                    Email email = Email.builder()
                            .recipient(user.getEmail())
                            .msgBody("localhost:8080/api/users/activate/" + user.getUuid())
                            .subject("Activate Account")
                            .build();
                    mailService.sendSimpleMail(email);
                    return ServerResponse.ok().body(Mono.just(user.getUuid()), String.class);
                });
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        if (JWT == null) {
            return Mono.error(new Exception("Not found JWT"));
        }
        String userId = tokenProvider.getUserIdFromToken(JWT);
        LOGGER.info("Hit API updateProfile with user ID: {}", userId);
        return Mono
                .zip(
                        (data) -> {
                            User user = (User) data[0];
                            UpdateProfileRequest input = (UpdateProfileRequest) data[1];
                            user.updateProfile(input);
                            return user;
                        },
                        userRepository.findById(userId),
                        request.bodyToMono(UpdateProfileRequest.class)
                )
                .cast(User.class)
                .flatMap(userRepository::save)
                .flatMap(user -> ServerResponse.ok().body(Mono.just(user), User.class));
    }

    public Mono<ServerResponse> activate(ServerRequest request) {
        LOGGER.info("Hit API active user ID: {}", request.pathVariable("id"));
        return userRepository.findById(request.pathVariable("id"))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Not found user")))
                .flatMap(user -> Mono.just(user.activate()))
                .flatMap(userRepository::save)
                .flatMap(user -> ServerResponse.ok().build());
    }

    public enum Action {
        ACTIVATE,
        DEACTIVATE
    }
}
