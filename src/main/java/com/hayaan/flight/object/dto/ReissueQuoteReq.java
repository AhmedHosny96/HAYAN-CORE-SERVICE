package com.hayaan.flight.object.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReissueQuoteReq {

    private String uniqueReference;
    private LocalDate departureDate;
    private String flightNumber;
    private Cabin cabin;

    enum Cabin{
        Economy,Business
    }
}


