package com.hayaan.flight.object.dto.flight;

import java.time.LocalDate;
import java.util.List;

public record FlightSearchDto(

        int noOfAdult,
        int noOfChildren,
        int noOfInfant,
        LocalDate departureDate,
        LocalDate returnDate,
        String from,
        String to,
//        boolean includeFlightDetails,
        String preferredCabin, // Business or economy
//        boolean includePriceModifier,
        String currency
) {
}


//            "carrier": "ET",
//                    "flightNumber": "210",
//                    "equipment": "DH8",
//                    "departureDate": "2024-02-29",
//                    "departureTime": "07:00:00.000+03:00",
//                    "arrivalDate": "2024-02-29",
//                    "arrivalTime": "08:20:00.000+03:00",
//                    "origin": "ADD",
//                    "destination": "JIJ"