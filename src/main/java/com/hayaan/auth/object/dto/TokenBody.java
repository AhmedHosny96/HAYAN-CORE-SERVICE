package com.hayaan.auth.object.dto;

public record TokenBody(
        Integer roleId, Long userId, Long agentId

) {
}
