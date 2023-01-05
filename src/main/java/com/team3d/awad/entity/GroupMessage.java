package com.team3d.awad.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("group_message")
public class GroupMessage {

    @Id
    private String uuid;

    private String senderId;

    private String sender;

    private String content;

    private Date createdTime;

    private String groupId;
}
