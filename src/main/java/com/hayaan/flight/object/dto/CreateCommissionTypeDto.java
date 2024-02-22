package com.hayaan.flight.object.dto;

import lombok.Builder;
import lombok.Data;

public record CreateCommissionTypeDto(
        String type
) {
    public static class AirPriceInfoResponse {
    }

    @Data
    @Builder
    public static class FlightSearchFullResponse {

        private int status;
        private String message;


    }
}
