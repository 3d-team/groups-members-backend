package com.team3d.awad.payload;

import lombok.*;
import org.springframework.context.annotation.Bean;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateClassRequest {

    private String className;

    private String section;

    private String subjectName;
}
