package com.hayaan.flight.object.dto.flight;

import com.sun.istack.NotNull;

import java.time.LocalDate;

public record FlightSearchDto(

        @NotNull int noOfAdult, @NotNull int noOfChildren, @NotNull int noOfInfant,
        @NotNull LocalDate departureDate, LocalDate returnDate, @NotNull String from,
        @NotNull String to, String preferredCabin,
        @NotNull String currency, String airLineCode

) {
}


