package com.hayaan.flight.controller;


import com.hayaan.flight.object.dto.booking.BookingPaymentDto;
import com.hayaan.flight.service.FlightPaymentService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentController {

    private final FlightPaymentService flightPaymentService;


    // 1X0999

    @PostMapping(value = "/flight/payment", produces = "application/json")
    public ResponseEntity<?> ticketPayment(@RequestBody BookingPaymentDto bookingPaymentDto) {

        JSONObject response = flightPaymentService.makePayment(bookingPaymentDto);

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }
}
