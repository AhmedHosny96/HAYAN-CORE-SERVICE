package com.hayaan.auth.object.dto;

import java.time.LocalDateTime;

public record CreateUserDto(
        String username,
        String email,
        String phoneNumber,
        String fullName,
        Long agentId,
        int roleId,
        String createdBy
) {
}
