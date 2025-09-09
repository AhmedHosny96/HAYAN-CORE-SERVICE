package com.hayaan.auth.object.dto;

import lombok.Data;

@Data
public class UpdateUserDto {
    
    private String username;
    private String email;
    private String phoneNumber;
    private String fullName;
    private Long roleId;
    private Integer status;
} 