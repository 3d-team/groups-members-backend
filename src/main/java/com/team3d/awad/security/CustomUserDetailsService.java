package com.team3d.awad.security;

import com.team3d.awad.exception.UsernameNotFoundException;
import com.team3d.awad.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
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

    public Mono<UserDetails> findByUserId(String uuid) {
        LOGGER.info("Hit {}", CustomUserDetailsService.class);
        return userRepository.findById(uuid)
                .switchIfEmpty(Mono.error(new Exception("User ID not found")))
                .flatMap(user -> Mono.just(CustomUserDetails.create(user)));
    }
}
