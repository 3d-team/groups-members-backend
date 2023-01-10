package com.team3d.awad.router.handler;

import com.team3d.awad.entity.User;
import com.team3d.awad.payload.Email;
import com.team3d.awad.payload.RegisterUserRequest;
import com.team3d.awad.payload.UpdateProfileRequest;
import com.team3d.awad.repository.UserRepository;
import com.team3d.awad.security.TokenProvider;
import com.team3d.awad.service.MailService;
import com.team3d.awad.utils.PasswordHasher;
import com.team3d.awad.utils.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class UserHandler {

    private static final Logger LOGGER = LogManager.getLogger(UserHandler.class);
    private final UserRepository userRepository;
    private final MailService mailService;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHasher hasher;

    public UserHandler(UserRepository userRepository,
                       MailService mailService,
                       TokenProvider tokenProvider,
                       PasswordEncoder passwordEncoder, PasswordHasher hasher) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.hasher = hasher;
    }

    public Mono<ServerResponse> all(ServerRequest request) {
        return ServerResponse.ok().build();
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        LOGGER.info("[*] Hit API #GetUser with, id: {}", request.pathVariable("id"));
        return userRepository.findById(request.pathVariable("id"))
                .flatMap(user -> ServerResponse.ok().body(Mono.just(user), User.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> profile(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        return userRepository.findById(userId)
                .flatMap(user -> ServerResponse.ok().body(Mono.just(user), User.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(RegisterUserRequest.class)
                .flatMap(payload -> {
                    LOGGER.info("[*] Hit API #Register New User with email: {}", payload.getEmail());
                    User user = User.builder()
                            .fullName(payload.getFullName())
                            .email(payload.getEmail())
                            .password(hasher.hash(payload.getPassword()))
                            .studentId(payload.getStudentId())
                            .build();
                    LOGGER.info(user);
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
        LOGGER.info("[*] Hit API #Update Profile, with user ID: {}", userId);
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
        LOGGER.info("[*] Hit API #Active User ID: {}", request.pathVariable("id"));
        return userRepository.findById(request.pathVariable("id"))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Not found user")))
                .flatMap(user -> Mono.just(user.activate()))
                .flatMap(userRepository::save)
                .flatMap(user -> ServerResponse.created(URI.create("http://localhost:3000/")).build());
    }

    public enum Action {
        ACTIVATE,
        DEACTIVATE
    }
}
