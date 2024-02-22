package com.hayaan.flight.object.dto.flight;

import java.time.LocalDate;
import java.util.List;

public record FlightSearchDto(
        int pageNumber,
        List<PassengerCriteria> passengerCriteria,
        LocalDate departureDate,
        String from,
        String to,
        boolean includeFlightDetails,
        String preferredCabin, // Business or economy
        boolean includePriceModifier,
        String currency


) {
}

//   String passengerType,
//        int noOfPassengers,

// 1. basic search ✅
//{
//        "offersPerPage":1,
//        "passengerCriteria":[
//        {
//        "value":"ADT",
//        "number":2
//        }
//        ],
//        "searchCriteria":[
//        {
//        "departureDate":"2024-02-18",
//        "from":"ADD",
//        "to":"JIJ"
//        }
//        ]
//        }

// 2.with air search modifier add ✅

//    "searchModifiers" : {
//            "includeFlightDetails" : true
//            }


// 3. with airleg // or cabin selection add the object inside the search criteria ✅

// "modifiers": {
//         "allowDirectAccess": true,
//         "maxConnectionTime": 24,
//         "preferredCabins": [
//         "Business"
//         ]
//         }


// 4. with price modifier add below object
//    "pricingModifiers" : {
//            "currencyCode" : "USD",
//            "airPricingModifiers" : {
//            "prohibitedNonRefundableFares" : true
//            }
//            }