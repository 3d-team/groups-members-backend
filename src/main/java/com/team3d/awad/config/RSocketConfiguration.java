package com.team3d.awad.config;

import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
public class RSocketConfiguration {

    @Bean
    public RSocketRequester rSocketRequester(RSocketStrategies strategies,
                                                   RSocketProperties properties) {
        return RSocketRequester.builder()
                .rsocketStrategies(strategies)
                .dataMimeType(MediaType.APPLICATION_JSON)
                .metadataMimeType(MediaType.APPLICATION_JSON)
                .websocket(uri(properties));
    }

    private URI uri(RSocketProperties properties) {
        return URI.create(String.format("ws://localhost:%d%s", 8080,
                properties.getServer().getMappingPath()));
    }
}
