package com.team3d.awad.router.handler;

import com.team3d.awad.entity.User;
import com.team3d.awad.payload.ChangePasswordRequest;
import com.team3d.awad.payload.Email;
import com.team3d.awad.payload.LoginRequest;
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

import java.util.Random;

@Component
public class AuthHandler {

    private static final Logger LOGGER = LogManager.getLogger(AuthHandler.class);

    private final UserRepository userRepository;

    private final TokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;

    private final PasswordHasher hasher;

    public AuthHandler(UserRepository userRepository,
                       TokenProvider tokenProvider,
                       PasswordEncoder passwordEncoder, MailService mailService, PasswordHasher hasher) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.hasher = hasher;
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(payload -> userRepository.findByEmail(payload.getEmail())
                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Email not found")))
                        .flatMap(user -> {
                            LOGGER.info("[*] Hit API #Login with email: {}", user.getEmail());
                            if (hasher.authenticate(payload.getPassword(), user.getPassword())) {
                                String token = tokenProvider.createToken(user.getUuid());
                                LOGGER.info("[i] User: {}, JWT: {}", user.getEmail(), token);
                                return ServerResponse.ok()
                                        .body(Mono.just(token), String.class);
                            }

                            return ServerResponse.notFound().build();
                        }));
    }

    public Mono<ServerResponse> resetPassword(ServerRequest request) {
        return request.bodyToMono(String.class)
                .flatMap(email -> userRepository.findByEmail(email)
                        .flatMap(user -> {
                            String newPassword = alphaNumericString(8);
                            user.setPassword(hasher.hash(newPassword));
                            Email mail = Email.builder()
                                    .recipient(email)
                                    .msgBody("New password: " + newPassword + " (Please do not share with anyone).")
                                    .subject("Reset password")
                                    .build();
                            mailService.sendSimpleMail(mail);
                            return Mono.just(user);
                        }))
                .flatMap(userRepository::save)
                .flatMap(user -> ServerResponse.ok().body(Mono.just(user), User.class))
                .switchIfEmpty(ServerResponse.badRequest().build());
    }

    public String alphaNumericString(int len) {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rnd = new Random();

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    public Mono<ServerResponse> changePassword(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        if (userId == null) {
            return ServerResponse.badRequest().build();
        }

        return request.bodyToMono(ChangePasswordRequest.class)
                .flatMap(payload -> userRepository.findById(userId)
                        .flatMap(user -> {
                            String oldPassword = payload.getOldPassword();
                            if (hasher.authenticate(oldPassword, user.getPassword())) {
                                user.setPassword(hasher.hash(payload.getNewPassword()));
                                return ServerResponse.ok().body(userRepository.save(user), User.class);
                            }
                            return ServerResponse.badRequest().build();
                        }))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
