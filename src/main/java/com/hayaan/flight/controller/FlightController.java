package com.hayaan.flight.controller;


import com.hayaan.flight.object.dto.AirPriceSolution;
import com.hayaan.flight.object.dto.booking.BookingRequestDto;
import com.hayaan.flight.object.dto.booking.BookingResponse;
import com.hayaan.flight.object.dto.booking.FlightByPnrCodeResponse;
import com.hayaan.flight.object.dto.booking.FlightByPnrDto;
import com.hayaan.flight.object.dto.flight.FlightPriceSearchDto;
import com.hayaan.flight.object.dto.flight.FlightSearchDto;
import com.hayaan.flight.object.dto.flight.FlightSearchResponse;
import com.hayaan.flight.service.TravelPortService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FlightController {


    private final TravelPortService travelPortService;

    // GET FLIGHT BY PNR
    @GetMapping("/flight")
    public ResponseEntity<FlightByPnrCodeResponse> getFlightByPnrCode(@RequestParam("pnrCode") String pnrCode) {
        FlightByPnrCodeResponse flightByPnr = travelPortService.findFlightByPnr(pnrCode);
        return new ResponseEntity<>(flightByPnr, HttpStatusCode.valueOf(flightByPnr.getStatus()));
    }

    // SEARCH AVAILABLE FLIGHTS
    @PostMapping("/flights/availability")
    public ResponseEntity<FlightSearchResponse> searchAllAvailableFlights(@RequestBody FlightSearchDto flightSearchDto) {
        FlightSearchResponse flightSearchResponse = travelPortService.searchFlights(flightSearchDto);
        return new ResponseEntity<>(flightSearchResponse, HttpStatusCode.valueOf(flightSearchResponse.getStatus()));
    }

    // SEARCH SINGLE FLIGHT
    @PostMapping("/flight/detail")
    public ResponseEntity<AirPriceSolution> searchFlight(@RequestBody FlightPriceSearchDto flightSearchDto) {
        AirPriceSolution airPriceSolution = travelPortService.searchFlightWithPrice(flightSearchDto);
        return new ResponseEntity<>(airPriceSolution, HttpStatusCode.valueOf(airPriceSolution.getStatus()));
    }

    // BOOK FLIGHT
    @PostMapping("/flight/booking")
    public ResponseEntity<?> bookFlight(@RequestBody BookingRequestDto bookingRequestDto) {
        BookingResponse bookingResponse = travelPortService.bookFlight(bookingRequestDto);
        return new ResponseEntity<>(bookingResponse, HttpStatusCode.valueOf(bookingResponse.getStatus()));
    }


}
