package com.hayaan.flight.object.dto.flight;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FlightSearchDto(

        @NotNull @NotBlank int noOfAdult, @NotNull @NotBlank int noOfChildren, @NotNull @NotBlank int noOfInfant,
        @NotNull @NotBlank LocalDate departureDate, LocalDate returnDate, @NotNull @NotBlank String from,
        @NotNull @NotBlank String to, String preferredCabin,
        @NotNull @NotBlank(message = "Currency can't be empty") String currency, String airLineCode

) {
}


