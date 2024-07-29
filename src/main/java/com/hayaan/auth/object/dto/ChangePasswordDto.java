package com.hayaan.auth.object.dto;


public record ChangePasswordDto(
        String oldPassword,
        String newPassword
) {
}
