package com.hayaan.auth.object.dto;

import com.hayaan.auth.object.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class AllUserResp {

    private int status;
    private String message;
    private List<User> users;
}
