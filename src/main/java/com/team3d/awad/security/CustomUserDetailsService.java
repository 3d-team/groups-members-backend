package com.team3d.awad.security;

import com.team3d.awad.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private static final Logger LOGGER = LogManager.getLogger(CustomUserDetails.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        LOGGER.info("Hit {}", CustomUserDetailsService.class);
        return userRepository.findByEmail(email)
                .flatMap(user -> Mono.just(CustomUserDetails.create(user)));
    }
}
