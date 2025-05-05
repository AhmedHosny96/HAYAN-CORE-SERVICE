package com.hayaan.auth.object.dto;

import com.hayaan.auth.object.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RolesResponse {

    private int status;
    private String message;

    private List<Role> roles;
}
