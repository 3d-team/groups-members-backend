package com.team3d.awad.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateMessageRequest {

    private String sender;

    private String content;

    private Date createdTime;

    private String groupId;
}
