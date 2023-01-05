package com.team3d.awad.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3d.awad.manager.GroupChatManager;
import com.team3d.awad.manager.SharingPresentationManager;
import com.team3d.awad.payload.metadata.SubscribeGroupChatMetadata;
import com.team3d.awad.payload.metadata.SubscribePresentationMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class RSocketController {

    private final static Logger LOGGER = LogManager.getLogger(RSocketController.class);
    private final ObjectMapper mapper;

    private final SharingPresentationManager presentationManager;

    private final GroupChatManager groupChatManager;

    private final Flux<String> stream;

    public RSocketController(ObjectMapper mapper, SharingPresentationManager presentationManager, GroupChatManager groupChatManager, Flux<String> stream) {
        this.mapper = mapper;
        this.presentationManager = presentationManager;
        this.groupChatManager = groupChatManager;
        this.stream = stream;
    }

    @ConnectMapping("presentation:join")
    public void onConnect(SubscribePresentationMetadata metadata, RSocketRequester requester) {
        String fixedClientId = metadata.getClientId().replace("\"", "");
        String presentationId = metadata.getPresentationId();
        requester.rsocket()
                .onClose()
                .subscribe(null, null, () -> {
                        LOGGER.info("[rws] Client: {}, just disconnected", fixedClientId);
                        presentationManager.outPresentation(presentationId, fixedClientId);
                });
        LOGGER.info("[rws] Client: {}, connected, metadata: {}", fixedClientId, metadata);
        presentationManager.joinPresentation(presentationId, fixedClientId, requester);
    }

    @MessageMapping("presentation:update")
    public Flux<String> updatePresentation(SubscribePresentationMetadata metadata, RSocketRequester requester) {
        String fixedClientId = metadata.getClientId().replace("\"", "");
        String presentationId = metadata.getPresentationId();
        LOGGER.info("[rws] Listen to presentation changes, presentationId: {}", presentationId);
        presentationManager.joinPresentation(presentationId, fixedClientId, requester);
        return stream;
    }

    @ConnectMapping("chat:join")
    public void onConnect(SubscribeGroupChatMetadata metadata, RSocketRequester requester) {
        String fixedClientId = metadata.getClientId().replace("\"", "");
        String groupId = metadata.getGroupId();
        requester.rsocket()
                .onClose()
                .subscribe(null, null, () -> {
                    LOGGER.info("[rws] Client: {}, just disconnected", fixedClientId);
                    groupChatManager.outGroupChat(groupId, fixedClientId);
                });
        LOGGER.info("[rws] Client: {}, connected, metadata: {}", fixedClientId, metadata);
        groupChatManager.joinGroupChat(groupId, fixedClientId, requester);
    }

    @MessageMapping("chat:update")
    public Flux<String> updatePresentation(SubscribeGroupChatMetadata metadata, RSocketRequester requester) {
        String fixedClientId = metadata.getClientId().replace("\"", "");
        String groupId = metadata.getGroupId();
        LOGGER.info("[rws] Listen to new message from groupId: {}", groupId);
        return stream;
    }
}
