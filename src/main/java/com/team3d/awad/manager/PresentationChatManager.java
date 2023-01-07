package com.team3d.awad.manager;

import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PresentationChatManager {

    private final Map<String, Map<String, RSocketRequester>> presentationChats = new ConcurrentHashMap<>();

    public void joinGroupChat(String presentationId, String clientId, RSocketRequester requester) {
        if (!presentationChats.containsKey(presentationId)) {
            presentationChats.put(presentationId, new HashMap<>());
        }
        Map<String, RSocketRequester> requesters = presentationChats.get(presentationId);
        if (requesters.containsKey(clientId)) {
            return;
        }
        requesters.put(clientId, requester);
        presentationChats.put(presentationId, requesters);
    }

    public void outGroupChat(String presentationId, String clientId) {
        if (!presentationChats.containsKey(presentationId)) {
            return;
        }
        Map<String, RSocketRequester> requesters = presentationChats.get(presentationId);
        if (!requesters.containsKey(clientId)) {
            return;
        }
        requesters.remove(clientId);
        presentationChats.put(presentationId, requesters);
    }

    public void sendUpdate(String presentationId, String JSON) {
        if (!presentationChats.containsKey(presentationId)) {
            return;
        }
        Map<String, RSocketRequester> requesters = presentationChats.get(presentationId);
        requesters.values().forEach(requester -> send(requester, JSON));
    }

    public void send(RSocketRequester requester, String JSON) {
        requester.route("chat:update").data(JSON).send().subscribe();
    }
}
