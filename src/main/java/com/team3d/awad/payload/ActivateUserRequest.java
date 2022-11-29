package com.team3d.awad.payload;

import com.team3d.awad.router.handler.UserHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivateUserRequest {

    private UserHandler.Action action;

    private String userId;
}
