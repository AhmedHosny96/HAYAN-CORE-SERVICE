package com.hayaan.auth.object.dto;

public record AuthResponse(
        int status,
        String message,
        String username,
        String token
) {
}
