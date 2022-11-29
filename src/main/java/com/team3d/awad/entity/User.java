package com.team3d.awad.entity;

import com.team3d.awad.security.AuthProvider;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class User {

    @Id
    private String uuid;

    private String username;

    private String email;

    private String password;

    @Builder.Default
    private String fullName = "";

    private Date dob;

    @Builder.Default
    private AuthProvider provider = AuthProvider.local;

    private String providerId;

    @DocumentReference(lazy = true)
    private List<Role> roles;
}
