package com.hayaan.flight.object.dto;


public record AgentStatusChangeDto(
        Long agentId,
        int status
) {
}
