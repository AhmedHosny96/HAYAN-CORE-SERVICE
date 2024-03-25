package com.hayaan.flight.controller;


import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.AirPriceSolution;
import com.hayaan.flight.object.dto.AirportListResp;
import com.hayaan.flight.object.dto.booking.BookingRequestDto;
import com.hayaan.flight.object.dto.booking.BookingResponse;
import com.hayaan.flight.object.dto.booking.FlightByPnrCodeResponse;
import com.hayaan.flight.object.dto.booking.FlightByPnrDto;
import com.hayaan.flight.object.dto.flight.FlightPriceSearchDto;
import com.hayaan.flight.object.dto.flight.FlightSearchDto;
import com.hayaan.flight.object.dto.flight.FlightSearchResponse;
import com.hayaan.flight.service.FlightLogicService;
import com.hayaan.flight.service.TravelPortService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
//@Tag(name = "Flight Booking APIs", description = "Flight Management APIs")
public class FlightController {

    private final TravelPortService travelPortService;
    private final FlightLogicService flightLogicService;


    @GetMapping("/airports")
//    @Operation(
//            summary = "Retrieve Airports",
//            description = "Get All International Airports",
//            tags = {"airports", "GET"}
//    )
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = AirportListResp.class), mediaType = "application/json")}),
//            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema(implementation = AirportListResp.class))}),
//            @ApiResponse(responseCode = "500", content = {@Content(schema = @Schema())})})

    public ResponseEntity<AirportListResp> getAirports() {
        AirportListResp allAirports = flightLogicService.getAllAirports();
        return new ResponseEntity<>(allAirports, HttpStatusCode.valueOf(allAirports.getStatus()));
    }

    @GetMapping("/airlines")
    @Tag(name = "Airlines", description = "Fetch all airlines")
    public ResponseEntity<AirportListResp> getAirlines() {
        AirportListResp allAirlines = flightLogicService.getAllAirlines();
        return new ResponseEntity<>(allAirlines, HttpStatusCode.valueOf(allAirlines.getStatus()));
    }


    @GetMapping("/flight/confirm-ticket")
    @Tag(name = "Flight Booking", description = "Confirm ticket booking")
    public ResponseEntity<CustomResponse> confirmTicket(@RequestParam("pnrCode") String pnrCode) {
        CustomResponse confirmTicketResponse = flightLogicService.confirmTicket(pnrCode);
        return new ResponseEntity<>(confirmTicketResponse, HttpStatusCode.valueOf(confirmTicketResponse.status()));
    }


    @GetMapping("/flight")
    @Tag(name = "Flight Details", description = "Retrieve flight details by PNR code")
    public ResponseEntity<FlightByPnrCodeResponse> getFlightByPnrCode(@RequestParam("pnrCode") String pnrCode) {
        FlightByPnrCodeResponse flightByPnr = travelPortService.findFlightByPnr(pnrCode);
        return new ResponseEntity<>(flightByPnr, HttpStatusCode.valueOf(flightByPnr.getStatus()));
    }


    @PostMapping("/flights/availability")
    @Tag(name = "Flight Availability", description = "Search all available flights")
    public ResponseEntity<FlightSearchResponse> searchAllAvailableFlights(@RequestBody FlightSearchDto flightSearchDto) {
        FlightSearchResponse flightSearchResponse = flightLogicService.searchFlight(flightSearchDto);
        return new ResponseEntity<>(flightSearchResponse, HttpStatusCode.valueOf(flightSearchResponse.getStatus()));
    }


    @PostMapping("/flight/detail")
    @Tag(name = "Flight Detail", description = "Search single flight detail")
    public ResponseEntity<AirPriceSolution> searchFlight(@RequestBody FlightPriceSearchDto flightSearchDto) {
        AirPriceSolution airPriceSolution = travelPortService.searchFlightWithPrice(flightSearchDto);
        return new ResponseEntity<>(airPriceSolution, HttpStatusCode.valueOf(airPriceSolution.getStatus()));
    }

    @PostMapping("/flight/booking")
    @Tag(name = "Flight Booking", description = "Book a flight")
    public ResponseEntity<?> bookFlight(@RequestBody BookingRequestDto bookingRequestDto) {
        BookingResponse bookingResponse = flightLogicService.bookFlight(bookingRequestDto);
        return new ResponseEntity<>(bookingResponse, HttpStatusCode.valueOf(bookingResponse.getStatus()));
    }

}
