package com.team3d.awad.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3d.awad.entity.Message;
import com.team3d.awad.entity.Presentation;
import com.team3d.awad.repository.PresentationRepository;
import com.team3d.awad.ws.PresentationHostHandler;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Component
public class SharingPresentationManager {

    private static final String PRESENTATION_ENDPOINT = "presentation:join";

    private static final String PRESENTATION_STREAM = "presentation:update";

    private static final String CHAT_ENDPOINT = "chat:join";

    private static final String CHAT_STREAM = "chat:update";

    private static final Logger LOGGER = LogManager.getLogger(PresentationHostHandler.class);

    private Map<String, SharingPresentationSession> shares = new ConcurrentHashMap<>();

    private final PresentationRepository presentationRepository;

    private final ObjectMapper mapper;

    public SharingPresentationManager(PresentationRepository presentationRepository, ObjectMapper mapper) {
        this.presentationRepository = presentationRepository;
        this.mapper = mapper;
    }

    public Mono<String> shareNewPresentation(String presentationId) {
        return presentationRepository.findById(presentationId)
                .flatMap(presentation -> {
                    Map<String, Choice> slides = presentation.getSlides().stream()
                            .collect(Collectors.toMap(
                                    Presentation.Slide::getUuid,
                                    slide -> {
                                        Map<String, List<String>> choices = slide.getOptions()
                                                .stream().collect(Collectors.toMap(
                                                        Presentation.SlideOption::getUuid,
                                                        option -> new ArrayList<>(),
                                                        (o1, o2) -> o1));
                                        return Choice.builder().choices(choices).build();
                                    })
                            );
                    SharingPresentationSession session = SharingPresentationSession.builder()
                            .presentationId(presentationId)
                            .slides(slides)
                            .build();
                    shares.put(presentationId, session);
                    return Mono.just(session.getUuid());
                });
    }

    public void updatePresentationSession(Presentation presentation) {
        if (!shares.containsKey(presentation.getUuid())) {
            return;
        }
        SharingPresentationSession session = shares.get(presentation.getUuid());
        session.update(presentation);
    }

    public void outPresentation(String presentationId, String clientId) {
        if (!shares.containsKey(presentationId)) {
            return;
        }
        SharingPresentationSession session = shares.get(presentationId);
        session.outPresentation(clientId);
    }

    public void joinPresentation(String presentationId, String clientId, RSocketRequester requester) {
        if (!shares.containsKey(presentationId)) {
            return;
        }
        SharingPresentationSession session = shares.get(presentationId);
        session.joinPresentation(clientId, requester);
    }

    public void publishPresentationUpdate(Presentation presentation) {
        String presentationId = presentation.getUuid();
        if (!shares.containsKey(presentationId)) {
            return;
        }
        SharingPresentationSession session = shares.get(presentationId);
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("session", session);
            payload.put("presentation", presentation);
            String JSON = mapper.writeValueAsString(payload);

            session.sendBroadcast(JSON, PRESENTATION_STREAM);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can't serialize Presentation");
            throw new RuntimeException(e);
        }
    }

    public void publishPresentationSession(String presentationId) {
        if (!shares.containsKey(presentationId)) {
            return;
        }
        SharingPresentationSession session = shares.get(presentationId);
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("session", session);
            String JSON = mapper.writeValueAsString(payload);

            LOGGER.info("Puplish Session");
            session.sendBroadcast(JSON, PRESENTATION_STREAM);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can't serialize Presentation");
            throw new RuntimeException(e);
        }
    }

    public void publishNewMessage(String presentationId, Message message) {
        if (!shares.containsKey(presentationId)) {
            return;
        }
        LOGGER.info("Publish new message to {}", presentationId);
        SharingPresentationSession session = shares.get(presentationId);
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("session", session);
            payload.put("message", message);
            String JSON = mapper.writeValueAsString(payload);

            session.sendBroadcast(JSON, CHAT_STREAM);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can't serialize Presentation");
            throw new RuntimeException(e);
        }
    }

    public void voting(String presentationId, String optionId, String clientId) {
        if (!shares.containsKey(presentationId)) {
            return;
        }
        SharingPresentationSession session = shares.get(presentationId);
        session.voting(optionId, clientId);
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class SharingPresentationSession {

        @Builder.Default
        private String uuid = UUID.randomUUID().toString();

        private String presentationId;

        @Builder.Default
        private final Map<String, RSocketRequester> requesters = new HashMap<>();

        private Map<String, Choice> slides;

        public void joinPresentation(String clientId, RSocketRequester requester) {
            if (requesters.containsKey(clientId)) {
                return;
            }
            requesters.put(clientId, requester);
        }

        public void outPresentation(String clientId) {
            if (!requesters.containsKey(clientId)) {
                return;
            }
            requesters.remove(clientId);
            for (Map.Entry<String, Choice> entry : slides.entrySet()) {
                Choice choice = entry.getValue();
//                choice.removeClientChoice(clientId);
            }
        }

        public void update(Presentation presentation) {
            List<Presentation.Slide> newSlides = presentation.getSlides();
            for (Presentation.Slide slide : newSlides) {
                if (!slides.containsKey(slide.getUuid())) {
                    Map<String, List<String>> choices = slide.getOptions()
                            .stream().collect(Collectors.toMap(
                                    Presentation.SlideOption::getUuid,
                                    option -> new ArrayList<>(),
                                    (o1, o2) -> o1));
                    Choice choice = Choice.builder().choices(choices).build();
                    slides.put(slide.getUuid(), choice);
                    continue;
                }

                Choice choice = slides.get(slide.getUuid());
                choice.update(slide);
                slides.put(slide.getUuid(), choice);
            }
        }

        public void voting(String optionId, String clientId) {
            slides.values().forEach(choice -> choice.choose(optionId, clientId));
        }

        public void sendBroadcast(String JSON, String route) {
            LOGGER.info("[rws] Send broadcast all listeners, presentation: {}", JSON);
            requesters.forEach((key, value) -> sendClient(key, JSON, route));
        }

        public void sendClient(String clientId, String JSON, String route) {
            RSocketRequester requester = requesters.get(clientId);
            if (requester == null) {
                return;
            }
            LOGGER.info("[>] Send update to client, clientId: {}", clientId);
            requester.route(route).data(JSON).send().subscribe();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Choice {

        @Builder.Default
        private String uuid = UUID.randomUUID().toString();
        private Map<String, List<String>> choices;

        public void choose(String optionId, String clientId) {
            List<String> choosers = choices.get(optionId);
            if (choosers == null) {
                return;
            }
            choosers.add(clientId);
            choices.put(optionId, choosers);
        }

        public void update(Presentation.Slide slide) {
            List<Presentation.SlideOption> options = slide.getOptions();
            for (Presentation.SlideOption option : options) {
                if (choices.containsKey(option.getUuid())) {
                    continue;
                }
                choices.put(option.getUuid(), new ArrayList<>());
            }
        }

        public void discard(String optionId, String clientId) {
            List<String> choosers = choices.get(optionId);
            if (!choosers.contains(clientId)) {
                return;
            }
            choosers.remove(clientId);
            choices.put(optionId, choosers);
        }

        public void removeClientChoice(String clientId) {
            for (String optionId: choices.keySet()) {
                discard(optionId, clientId);
            }
        }
    }
}
