package com.team3d.awad.payload.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubscribePresentationChatMetadata {

    private String presentationId;

    private String clientId;
}
