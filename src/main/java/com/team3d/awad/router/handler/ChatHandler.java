package com.team3d.awad.router.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3d.awad.entity.Message;
import com.team3d.awad.manager.PresentationChatManager;
import com.team3d.awad.manager.SharingPresentationManager;
import com.team3d.awad.payload.CreateMessageRequest;
import com.team3d.awad.repository.GroupMessageRepository;
import com.team3d.awad.security.TokenProvider;
import com.team3d.awad.utils.RequestUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ChatHandler {

    private final GroupMessageRepository groupMessageRepository;

    private final TokenProvider tokenProvider;

    private final PresentationChatManager groupChatManager;

    private final ObjectMapper mapper;

    private final SharingPresentationManager presentationManager;

    public ChatHandler(GroupMessageRepository groupMessageRepository, TokenProvider tokenProvider, PresentationChatManager groupChatManager, ObjectMapper mapper, SharingPresentationManager presentationManager) {
        this.groupMessageRepository = groupMessageRepository;
        this.tokenProvider = tokenProvider;
        this.groupChatManager = groupChatManager;
        this.mapper = mapper;
        this.presentationManager = presentationManager;
    }

    public Mono<ServerResponse> all(ServerRequest request) {
        return ServerResponse.ok().body(
                groupMessageRepository.findAllByPresentationIdOrderByCreatedDateDesc(request.pathVariable("id")),
                Message.class);
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        return request.bodyToMono(CreateMessageRequest.class)
                .flatMap(payload -> {
                    Message message = Message.builder()
                            .senderId(userId)
                            .sender(payload.getSender())
                            .content(payload.getContent())
                            .createdDate(payload.getCreatedDate())
                            .presentationId(payload.getPresentationId())
                            .build();
                    return Mono.just(message);
                })
                .flatMap(groupMessageRepository::save)
                .flatMap(message -> {
                    presentationManager.publishNewMessage(message.getPresentationId(), message);
                    return ServerResponse.ok().body(Mono.just(message), Message.class);
                });
    }
}
