package com.team3d.awad.router.handler;

import com.team3d.awad.entity.Presentation;
import com.team3d.awad.repository.PresentationRepository;
import com.team3d.awad.security.TokenProvider;
import com.team3d.awad.utils.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class PresentationHandler {

    private static final Logger LOGGER = LogManager.getLogger(PresentationHandler.class);

    private final TokenProvider tokenProvider;

    private final PresentationRepository presentationRepository;

    public PresentationHandler(TokenProvider tokenProvider, PresentationRepository presentationRepository) {
        this.tokenProvider = tokenProvider;
        this.presentationRepository = presentationRepository;
    }

    public Mono<ServerResponse> all(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        LOGGER.info("[*] Hit API #Get All Presentations, of user ID: {}", userId);
        return ServerResponse.ok().body(presentationRepository.findAllByHostId(userId),
                Presentation.class);
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        return presentationRepository.findById(request.pathVariable("id"))
                .flatMap(presentation -> ServerResponse.ok().body(presentation, Presentation.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        LOGGER.info("[*] Hit API #Create Presentation, of Host ID: {}", userId);
        return request.bodyToMono(String.class)
                .flatMap(presentationName -> {
                    Presentation presentation = Presentation.builder()
                            .name(presentationName)
                            .hostId(userId)
                            .build();
                    return Mono.just(presentation);
                })
                .flatMap(presentationRepository::save)
                .flatMap(presentation -> ServerResponse.ok().body(presentation, Presentation.class));
    }
}
