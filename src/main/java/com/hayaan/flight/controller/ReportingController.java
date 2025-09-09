package com.hayaan.flight.controller;


import com.hayaan.flight.object.dto.BookingReportResp;
import com.hayaan.flight.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ReportingController {

    private final ReportService reportService;

    @GetMapping("/agent/bookings")
    public ResponseEntity<BookingReportResp> getAgentBookings(@RequestParam Long agentId) {
        BookingReportResp allAgentBooking = reportService.getBookingsByAgent(agentId);
        return ResponseEntity.status(allAgentBooking.getStatus()).body(allAgentBooking);
    }

    @GetMapping("/customer/bookings")
    public ResponseEntity<BookingReportResp> getPassengerBookings(@RequestParam Long customerId) {
        BookingReportResp allAgentBooking = reportService.getBookingsByPassenger(customerId);
        return ResponseEntity.status(allAgentBooking.getStatus()).body(allAgentBooking);
    }

    @GetMapping("/bookings")
    public ResponseEntity<BookingReportResp> getAllBookings(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        BookingReportResp allAgentBooking = reportService.getAllBookingsByDate(startDate, endDate);
        return ResponseEntity.status(allAgentBooking.getStatus()).body(allAgentBooking);
    }
}
