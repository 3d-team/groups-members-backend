package com.team3d.awad.manager;

import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GroupChatManager {

    private final Map<String, Map<String, RSocketRequester>> groupChats = new ConcurrentHashMap<>();

    public void joinGroupChat(String groupId, String clientId, RSocketRequester requester) {
        if (!groupChats.containsKey(groupId)) {
            groupChats.put(groupId, new HashMap<>());
        }
        Map<String, RSocketRequester> requesters = groupChats.get(groupId);
        if (requesters.containsKey(clientId)) {
            return;
        }
        requesters.put(clientId, requester);
        groupChats.put(groupId, requesters);
    }

    public void outGroupChat(String groupId, String clientId) {
        if (!groupChats.containsKey(groupId)) {
            return;
        }
        Map<String, RSocketRequester> requesters = groupChats.get(groupId);
        if (!requesters.containsKey(clientId)) {
            return;
        }
        requesters.remove(clientId);
        groupChats.put(groupId, requesters);
    }

    public void sendUpdate(String groupId, String JSON) {
        if (!groupChats.containsKey(groupId)) {
            return;
        }
        Map<String, RSocketRequester> requesters = groupChats.get(groupId);
        requesters.values().forEach(requester -> send(requester, JSON));
    }

    public void send(RSocketRequester requester, String JSON) {
        requester.route("chat:update").data(JSON).send().subscribe();
    }
}
