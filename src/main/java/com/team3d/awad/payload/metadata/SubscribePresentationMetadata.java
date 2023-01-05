package com.team3d.awad.payload.metadata;

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
