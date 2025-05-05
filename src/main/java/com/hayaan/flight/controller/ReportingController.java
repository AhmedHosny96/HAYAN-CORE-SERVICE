package com.hayaan.flight.controller;


import com.hayaan.flight.object.dto.BookingByUsers;
import com.hayaan.flight.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ReportingController {

    private final ReportService reportService;

    @GetMapping("/agent/bookings")
    public ResponseEntity<BookingByUsers> getAgentBookings(@RequestParam Long agentId) {
        BookingByUsers allAgentBooking = reportService.getBookingsByAgent(agentId);
        return new ResponseEntity<>(allAgentBooking, HttpStatusCode.valueOf(allAgentBooking.getStatus()));
    }

    @GetMapping("/customer/bookings")
    public ResponseEntity<BookingByUsers> getPassengerBookings(@RequestParam Long customerId) {
        BookingByUsers allAgentBooking = reportService.getBookingsByPassenger(customerId);
        return new ResponseEntity<>(allAgentBooking, HttpStatusCode.valueOf(allAgentBooking.getStatus()));
    }
}
