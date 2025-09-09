package com.hayaan.auth.object.dto;

import lombok.Data;

@Data
public class GoogleAuthDto {
    
    private String googleToken;
    private String firstName;
    private String lastName;
    private String email;
    private String picture;
} 