package com.team3d.awad.service;

import com.team3d.awad.entity.Presentation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresentationService {

    private static final Logger LOGGER = LogManager.getLogger(PresentationService.class);

    private Map<String, Presentation> presentations = new ConcurrentHashMap<>();

    public void presentNewPresentation(Presentation presentation) {
        presentations.putIfAbsent(presentation.getUuid(), presentation);
    }

    public void endPresentation(String uuid) {
        if (!presentations.containsKey(uuid)) {
            return;
        }
        presentations.remove(uuid);
    }

    public void votePresentation(String uuid, String slideId, String option) {
        presentations.computeIfPresent(uuid, (id, presentation) -> {
            List<Presentation.Slide> slides = presentation.getSlides();
            return presentation;
        });
    }
}
