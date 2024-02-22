package com.hayaan.flight.controller;


import com.hayaan.flight.object.dto.AirPriceInfoResponse;
import com.hayaan.flight.object.dto.AirPriceSolution;
import com.hayaan.flight.object.dto.flight.FlightPriceSearchDto;
import com.hayaan.flight.object.dto.flight.FlightSearchDto;
import com.hayaan.flight.object.dto.flight.FlightSearchResponse;
import com.hayaan.flight.service.TravelPortService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FlightController {


    private final TravelPortService travelPortService;

    @PostMapping("/flights/availability")
    public ResponseEntity<?> searchAllAvailableFlights(@RequestBody FlightSearchDto flightSearchDto) {
        FlightSearchResponse flightSearchResponse = travelPortService.searchFlights(flightSearchDto);
        return new ResponseEntity<>(flightSearchResponse, HttpStatusCode.valueOf(flightSearchResponse.getStatus()));
    }

    @PostMapping("/flight/detail")
    public ResponseEntity<?> searchFlight(@RequestBody FlightPriceSearchDto flightSearchDto) {
        AirPriceSolution airPriceSolution = travelPortService.searchFlightWithPrice(flightSearchDto);
        return new ResponseEntity<>(airPriceSolution, HttpStatusCode.valueOf(airPriceSolution.getStatus()));
    }


}
