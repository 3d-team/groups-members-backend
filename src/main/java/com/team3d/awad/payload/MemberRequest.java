package com.team3d.awad.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberRequest {

    private Type type;

    private Object data;

    public enum Type {
        ADD_MEMBER
    }
}
