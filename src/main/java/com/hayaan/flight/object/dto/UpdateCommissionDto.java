package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.FlightType;

public record UpdateCommissionDto(

        double amount,
        String currency,
        FlightType flightType,
        Long commissionTypeId,
        Long userId

) {
}
