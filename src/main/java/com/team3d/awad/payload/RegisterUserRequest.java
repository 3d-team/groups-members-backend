package com.team3d.awad.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {

    private String email;

    private String password;

    private String retype;

    private String studentId;

    private String fullName;
}
