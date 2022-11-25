package com.team3d.awad.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document("profile")
public class Groups {

    @Id
    private String uuid;

    private String name;

    private String description;
}
