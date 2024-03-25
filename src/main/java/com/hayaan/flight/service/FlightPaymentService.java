package com.hayaan.flight.service;


import com.hayaan.config.AsyncHttpConfig;
import com.hayaan.flight.object.dto.PaymentStageDto;
import com.hayaan.flight.object.dto.booking.BookingPaymentDto;
import com.hayaan.flight.object.entity.Payment;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.PaymentRepository;
import com.hayaan.flight.repo.TicketHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.RequestBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightPaymentService {

    @Value("${payment.endpoint}")
    private String PAYMENT_ENDPOINT;

    private final AsyncHttpConfig asyncHttp;

    private final TicketHistoryRepo ticketHistoryRepo;

    private final PaymentRepository paymentRepository;


    // THIS WILL CALL PAYMENT GATEWAY

    private String pnr;
    private String accountNumber;
    private String paymentMode;


    public JSONObject makePayment(BookingPaymentDto bookingPaymentDto) {
        // get the flight details by pnr
        var customResponse = new JSONObject();

        Optional<TicketHistory> flightByPnr = ticketHistoryRepo.findByPnr(bookingPaymentDto.getPnr());

        if (!flightByPnr.isPresent()) {
            customResponse.put("status", "400");
            customResponse.put("message", "Pnr doesnt exist");

            return customResponse;
        }

        TicketHistory ticketHistory = flightByPnr.get();

        if (ticketHistory.getStatus() == 3) {
            customResponse.put("status", "400");
            customResponse.put("message", "Pnr expired , Kindly book again");

            return customResponse;
        }

        // get payment by pnr

        Optional<Payment> byPnrAndPaymentStatus = paymentRepository.findByPnrAndPaymentStatus(bookingPaymentDto.getPnr(), 0);

        if (!byPnrAndPaymentStatus.isPresent()) {
            customResponse.put("status", "400");
            customResponse.put("message", "Pnr doesnt exist");

            return customResponse;
        }

        Payment payment = byPnrAndPaymentStatus.get();

        payment.setPayerAccount(bookingPaymentDto.getPayerAccount());
        paymentRepository.save(payment);

        if (ticketHistory.getStatus() != 0) {
            customResponse.put("status", "400");
            customResponse.put("message", "Flight Pnr has invalid status  , Kindly check if the ticket is paid or not");

            return customResponse;
        }

        var paymentRequest = new JSONObject();
        paymentRequest.put("pnr", bookingPaymentDto.getPnr());
        paymentRequest.put("paymentMode", bookingPaymentDto.getPaymentMethod());

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(PAYMENT_ENDPOINT)
                .setBody(paymentRequest.toString());

        JSONObject paymentResponse = asyncHttp.sendRequest(requestBody);

        log.info("PAYMENT RESPONSE : {}", paymentResponse);

//        if (!paymentRequest.optString("response").equals("000")) {
//
//            customResponse.put("status", "500");
//            customResponse.put("message", "Payment is not successful , contact your system admin");
//
//            return customResponse;
//
//        }

        // update payment details
//        ticketHistory.setPaymentDate(LocalDateTime.now());
//        ticketHistory.setPaymentMethod(bookingPaymentDto.getPaymentMethod());
//        ticketHistory.setPaymentReference(paymentResponse.getString("paymentReference"));
//        ticketHistory.setStatus(1); // payed

        return paymentResponse;

    }


}
