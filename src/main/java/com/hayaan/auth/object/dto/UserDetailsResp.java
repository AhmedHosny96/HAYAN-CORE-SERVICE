package com.hayaan.auth.object.dto;


import com.hayaan.auth.object.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailsResp {

    private int status;
    private String message;
    private User user;
}
