package com.hayaan.flight.object.dto.flight;

import java.time.LocalDate;

public record FlightSearchDto(

        int noOfAdult,
        int noOfChildren,
        int noOfInfant,
        LocalDate departureDate,
        LocalDate returnDate,
        String from,
        String to,
        String preferredCabin,
        String currency,
        String airLineCode
        
) {
}


