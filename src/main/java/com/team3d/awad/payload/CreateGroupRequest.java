package com.team3d.awad.payload;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateGroupRequest {

    private String className;

    private String section;

    private String subjectName;
}
