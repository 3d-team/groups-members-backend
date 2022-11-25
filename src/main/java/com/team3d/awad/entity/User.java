package com.team3d.awad.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.team3d.awad.security.AuthProvider;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@AllArgsConstructor
@Builder
@Document
public class User {

    @Id
    private String uuid;

    private String username;

    private String email;

    @JsonIgnore
    private String password;

    private AuthProvider provider;

    private String providerId;
}
