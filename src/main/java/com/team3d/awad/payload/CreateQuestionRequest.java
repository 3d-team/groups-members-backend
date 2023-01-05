package com.team3d.awad.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateQuestionRequest {

    private String title;

    private String content;

    private String groupId;
}
