package com.team3d.awad.entity;

import com.team3d.awad.payload.UpdateProfileRequest;
import com.team3d.awad.security.AuthProvider;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.ArrayList;
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

    private String studentId = "";

    private String email;

    private String password;

    @Builder.Default
    private String fullName = "";

    @Builder.Default
    private Date dob = new Date();

    @Builder.Default
    private String provider = "local";

    @Builder.Default
    private String providerId = "";

    @Builder.Default
    private boolean emailVerified = false;

    @DocumentReference(lazy = true)
    @Builder.Default
    private List<Role> roles = new ArrayList<>();

    public User normalize() {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        return this;
    }

    public User activate() {
        normalize();
        emailVerified = true;
        return this;
    }

    public void updateProfile(UpdateProfileRequest request) {
        normalize();
        fullName = request.getFullName();
        studentId = request.getStudentId();
        dob = request.getDob();
    }
}
