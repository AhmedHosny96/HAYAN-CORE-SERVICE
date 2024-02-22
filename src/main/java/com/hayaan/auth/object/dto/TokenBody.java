package com.hayaan.auth.object.dto;

public record TokenBody(
        String username, String role, Long userId, Long agentId, Integer status

) {
}
