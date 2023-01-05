package com.team3d.awad.payload;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SubscribePresentationMetadata {

    private String clientId;

    private String presentationId;
}
