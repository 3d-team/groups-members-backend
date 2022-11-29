package com.team3d.awad.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Email {

    private String sender;

    private String recipient;

    private String msgBody;

    private String subject;
}
