package com.team3d.awad.payload;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

    private String oldPassword;

    private String newPassword;
}
