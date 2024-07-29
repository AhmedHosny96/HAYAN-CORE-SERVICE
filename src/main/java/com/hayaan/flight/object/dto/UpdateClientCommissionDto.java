package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.FlightType;

public record UpdateClientCommissionDto(
        Long userId, FlightType flightType, Double amount
) {
}
