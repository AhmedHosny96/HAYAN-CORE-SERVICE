package com.hayaan.flight.object.dto;


public record CreateCommissionDto(
//        int commissionTypeId,
        double amount,
        Integer flightType,
        Integer classType,
        Integer agentId
) {
}
