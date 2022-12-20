package com.team3d.awad.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("presentation")
public class Presentation {

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
    private List<Slide> slides = Arrays.asList(
            Slide.builder()
                    .uuid(UUID.randomUUID().toString())
                    .title("Slide 1")
                    .type("multiple-choice")
                    .backgroundImage("")
                    .build()
    );

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Slide {

        private String uuid;

        private String title;

        private String type;

        private String backgroundImage;

        @Builder.Default
        private List<String> options = new ArrayList<>();
    }
}
