package com.hayaan.flight.object.dto;


import com.hayaan.flight.object.FlightType;

public record CreateCommissionDto(
        double amount,
        FlightType flightType,
        Long commissionTypeId,
        Long userId
) {
}
