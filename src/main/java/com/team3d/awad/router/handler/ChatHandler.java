package com.team3d.awad.router.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3d.awad.entity.GroupMessage;
import com.team3d.awad.manager.GroupChatManager;
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

    private final GroupChatManager groupChatManager;

    private final ObjectMapper mapper;

    public ChatHandler(GroupMessageRepository groupMessageRepository, TokenProvider tokenProvider, GroupChatManager groupChatManager, ObjectMapper mapper) {
        this.groupMessageRepository = groupMessageRepository;
        this.tokenProvider = tokenProvider;
        this.groupChatManager = groupChatManager;
        this.mapper = mapper;
    }

    public Mono<ServerResponse> all(ServerRequest request) {
        return ServerResponse.ok().body(
                groupMessageRepository.findAllByGroupIdOrderByCreatedTimeDesc(request.pathVariable("id")),
                GroupMessage.class);
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        return request.bodyToMono(CreateMessageRequest.class)
                .flatMap(payload -> {
                    GroupMessage message = GroupMessage.builder()
                            .senderId(userId)
                            .sender(payload.getSender())
                            .content(payload.getContent())
                            .createdTime(payload.getCreatedTime())
                            .groupId(payload.getGroupId())
                            .build();
                    return Mono.just(message);
                })
                .flatMap(groupMessageRepository::save)
                .flatMap(message -> {
                    try {
                        groupChatManager.sendUpdate(message.getGroupId(), mapper.writeValueAsString(message));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                    return ServerResponse.ok().body(Mono.just(message), GroupMessage.class);
                });
    }
}
