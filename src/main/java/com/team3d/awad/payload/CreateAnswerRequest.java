package com.team3d.awad.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.xml.ws.soap.Addressing;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAnswerRequest {

    private String answerer;

    private String content;
}
