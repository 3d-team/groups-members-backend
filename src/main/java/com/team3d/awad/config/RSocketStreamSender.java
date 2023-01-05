package com.team3d.awad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Configuration
public class RSocketStreamSender {

    @Bean
    public Sinks.Many<String> sink() {
        return Sinks.many().replay().latest();
    }

    @Bean
    public Flux<String> streamFlux(Sinks.Many<String> sink) {
        return sink.asFlux();
    }
}
