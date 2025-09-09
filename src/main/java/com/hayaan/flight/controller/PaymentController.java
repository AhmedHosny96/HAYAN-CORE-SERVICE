package com.hayaan.flight.controller;


import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.PaymentMethodResp;
import com.hayaan.flight.object.dto.booking.BookingPaymentDto;
import com.hayaan.flight.service.FlightPaymentService;
import com.hayaan.payments.EdahabService;
import com.hayaan.payments.WaafiService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

public class PaymentController {

    private final FlightPaymentService flightPaymentService;
    private final EdahabService edahabService;
    private final WaafiService waafiService;

    @GetMapping("/payments")
    public ResponseEntity<?> getActivePaymentMethods() {
        PaymentMethodResp allActivePaymentMethods = flightPaymentService.getAllActivePaymentMethods();
        return ResponseEntity.ok(allActivePaymentMethods);
    }

    // 1X0999
    @PostMapping(value = "/flight/payment", produces = "application/json")
    public ResponseEntity<?> ticketPayment(@RequestBody BookingPaymentDto bookingPaymentDto) {

        if (bookingPaymentDto.getPaymentMethod().equals("EDAHAB")) {
            //edahabService.createInvoice(bookingPaymentDto.getCurrency(), )
            CustomResponse response = edahabService.createInvoice(bookingPaymentDto.getPnr(), bookingPaymentDto.getCurrency(), bookingPaymentDto.getPayerAccount());
            return new ResponseEntity<>(response, HttpStatus.valueOf(response.status()));
        } else if (bookingPaymentDto.getPaymentMethod().equals("WAAFI")) {
            CustomResponse response = waafiService.payWithWaafi(bookingPaymentDto.getPnr(), bookingPaymentDto.getPayerAccount());
            return new ResponseEntity<>(response, HttpStatus.valueOf(response.status()));
        }
        JSONObject response = flightPaymentService.makePayment(bookingPaymentDto);
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }


}
