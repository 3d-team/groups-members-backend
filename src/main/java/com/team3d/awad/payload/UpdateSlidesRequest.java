package com.team3d.awad.payload;

import com.team3d.awad.entity.Presentation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateSlidesRequest {
    private String type;

    private String title;

    private String paragraph;

    private String backgroundImage;

    private List<Presentation.SlideOption> options;
}
