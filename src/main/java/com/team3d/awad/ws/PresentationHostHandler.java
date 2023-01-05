package com.team3d.awad.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3d.awad.manager.SharingPresentationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresentationHostHandler implements WebSocketHandler {

    private static final Logger LOGGER = LogManager.getLogger(PresentationHostHandler.class);

    private final SharingPresentationManager sharingPresentationManager;
    private final ObjectMapper mapper;

    public PresentationHostHandler(SharingPresentationManager sharingPresentationManager, ObjectMapper mapper) {
        this.sharingPresentationManager = sharingPresentationManager;
        this.mapper = mapper;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return Mono.empty();
    }
}
