package com.team3d.awad.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class UpdateProfileRequest {

    private final String studentId;

    private final String fullName;

    private final Date dob;
}
