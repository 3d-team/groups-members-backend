package com.team3d.awad.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("presentation")
public class Presentation {

    @Id
    private String uuid;

    private String name;

    @Builder.Default
    private Date createDate = new Date();

    @Builder.Default
    private Date modifiedDate = new Date();

    @Builder.Default
    private List<String> groupIds = new ArrayList<>();

    private String hostId;

    @Builder.Default
    private String accessCode = UUID.randomUUID().toString();

    @Builder.Default
    private List<Slide> slides = defaultSlides();

    private static List<Slide> defaultSlides() {
        Slide slide = Slide.builder()
                .uuid(UUID.randomUUID().toString())
                .title("Slide 1")
                .type("multiple-choice")
                .build();
        List<Slide> slides = new ArrayList<>();
        slides.add(slide);
        return slides;
    }

    public void updateSlides(Slide[] payload) {
        List<Slide> slides = Stream.of(payload).collect(Collectors.toList());
        setSlides(slides);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString(includeFieldNames = false)
    public static class Slide {

        @Builder.Default
        private String uuid = UUID.randomUUID().toString();

        @Builder.Default
        private String title = "";

        @Builder.Default
        private String type = "multiple-choice";

        @Builder.Default
        private String backgroundImage = "";

        @Builder.Default
        private String paragraph = "";

        @ToString.Exclude
        @Builder.Default
        private List<SlideOption> options = defaultOptions();

        private static List<SlideOption> defaultOptions() {
            SlideOption option1 = SlideOption.builder().build();
            SlideOption option2 = SlideOption.builder().build();
            SlideOption option3 = SlideOption.builder().build();
            List<SlideOption> options = new ArrayList<>();
            options.add(option1);
            options.add(option2);
            options.add(option3);
            return options;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString(includeFieldNames = false)
    public static class SlideOption {

        @Builder.Default
        private String uuid = UUID.randomUUID().toString();

        @Builder.Default
        private String name = "";

        @Builder.Default
        private int value = 0;
    }
}
