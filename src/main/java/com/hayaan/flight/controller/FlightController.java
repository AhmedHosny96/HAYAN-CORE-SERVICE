package com.hayaan.flight.controller;


import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.*;
import com.hayaan.flight.object.dto.booking.BookingRequestDto;
import com.hayaan.flight.object.dto.booking.BookingResponse;
import com.hayaan.flight.object.dto.booking.FlightByPnrCodeResponse;
import com.hayaan.flight.object.dto.flight.FlightPriceSearchDto;
import com.hayaan.flight.object.dto.flight.FlightSearchDto;
import com.hayaan.flight.object.dto.flight.FlightSearchResponse;
import com.hayaan.flight.service.CountryService;
import com.hayaan.flight.service.FlightLogicService;
import com.hayaan.flight.service.TravelPortService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

//@Tag(name = "Flight Booking APIs", description = "Flight Management APIs")
public class FlightController {

    private final TravelPortService travelPortService;
    private final FlightLogicService flightLogicService;

    private final CountryService countryService;

    @GetMapping("/customer")
    public ResponseEntity<PassengerResp> getPassengerByPhone(@RequestParam String phone) {
        PassengerResp passengerByPhone = flightLogicService.getPassengerByPhone(phone);
        return new ResponseEntity<>(passengerByPhone, HttpStatus.valueOf(passengerByPhone.getStatus()));
    }

    @GetMapping("/countries")
    public ResponseEntity<AllCountryResp> getAllCountries() {
        AllCountryResp allCountries = countryService.getAllCountries();

        return ResponseEntity.status(allCountries.getStatus()).body(allCountries);

    }


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
        return ResponseEntity.status(allAirports.getStatus()).body(allAirports);
    }

    @GetMapping("/airlines")
    //@Tag(name = "Airlines", description = "Fetch all airlines")
    public ResponseEntity<AirlineListResp> getAirlines() {
        AirlineListResp allAirlines = flightLogicService.getAllAirlines();
        return ResponseEntity.status(allAirlines.getStatus()).body(allAirlines);
    }

    @GetMapping("/flight/confirm-ticket")
    //@Tag(name = "Flight Booking", description = "Confirm ticket booking")
    public ResponseEntity<CustomResponse> confirmTicket(@RequestParam("pnrCode") String pnrCode) {
        CustomResponse confirmTicketResponse = flightLogicService.confirmTicket(pnrCode);
        return ResponseEntity.status(confirmTicketResponse.status()).body(confirmTicketResponse);
    }

    @GetMapping("/flight/cancel")
    //@Tag(name = "Flight cancel", description = "Cancel ticket ")
    public ResponseEntity<CustomResponse> cancelFlight(@RequestParam("pnrCode") String pnrCode) {
        CustomResponse confirmTicketResponse = travelPortService.cancelFlight(pnrCode);
        return ResponseEntity.status(confirmTicketResponse.status()).body(confirmTicketResponse);
    }


    @GetMapping("/flight") // confirms ticket in travel port
    //@Tag(name = "Flight Details", description = "Retrieve flight details by PNR code")
    public ResponseEntity<FlightByPnrCodeResponse> getFlightByPnrCode(@RequestParam("pnrCode") String pnrCode) {
        FlightByPnrCodeResponse flightByPnr = flightLogicService.getTripInfo(pnrCode);
        return ResponseEntity.status(flightByPnr.getStatus()).body(flightByPnr);
    }


    @PostMapping("/flights/availability")
    //@Tag(name = "Flight Availability", description = "Search all available flights")
    public ResponseEntity<FlightSearchResponse> searchAllAvailableFlights(@RequestBody FlightSearchDto flightSearchDto) {
        FlightSearchResponse flightSearchResponse = flightLogicService.searchFlight(flightSearchDto);
        return ResponseEntity.status(flightSearchResponse.getStatus()).body(flightSearchResponse);
    }

    @PostMapping("/flight/detail")
    //@Tag(name = "Flight Detail", description = "Search single flight detail")
    public ResponseEntity<AirPriceSolution> searchFlight(@RequestBody FlightPriceSearchDto flightSearchDto) {
        AirPriceSolution airPriceSolution = travelPortService.searchFlightWithPrice(flightSearchDto);
        return ResponseEntity.status(airPriceSolution.getStatus()).body(airPriceSolution);
    }

    @PostMapping("/flight/booking")
    //@Tag(name = "Flight Booking", description = "Book a flight")
    public ResponseEntity<?> bookFlight(@RequestBody BookingRequestDto bookingRequestDto) {
        BookingResponse bookingResponse = flightLogicService.bookFlight(bookingRequestDto);
        return ResponseEntity.status(bookingResponse.getStatus()).body(bookingResponse);
    }

    // REISSUE TICKET / CHANGE TICKET
    @GetMapping("/flight/reissue")
    //@Tag(name = "Flight logic reissue ticket", description = "Flight logic reissue ticket")
    public ResponseEntity<ReissueTicketResponse> reissueTicket(@RequestParam String pnr, @RequestParam LocalDate departureDate) {
        ReissueTicketResponse reissueTicketResponse = flightLogicService.reissueTicket(pnr, departureDate);
        return ResponseEntity.status(reissueTicketResponse.getStatus()).body(reissueTicketResponse);
    }

    // REISSUING STATUS
    @GetMapping("/flight/reissue/status")
    //@Tag(name = "Flight logic reissue ticket", description = "Flight logic reissue ticket status")
    public ResponseEntity<ReissueTicketResponse> getFlight(@RequestParam String pnr) {
        ReissueTicketResponse reissueTicketResponse = flightLogicService.getReissueTicketStatus(pnr);
        return ResponseEntity.status(reissueTicketResponse.getStatus()).body(reissueTicketResponse);
    }

}
