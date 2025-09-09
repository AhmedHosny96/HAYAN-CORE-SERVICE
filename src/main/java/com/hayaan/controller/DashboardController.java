package com.hayaan.controller;

import com.hayaan.dto.DashboardResponse;
import com.hayaan.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/bookings/today")
    public ResponseEntity<DashboardResponse.BookingsCountResponse> getTodayBookings() {
        DashboardResponse.BookingsCountResponse response = dashboardService.getTodayBookings();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/customers/total")
    public ResponseEntity<DashboardResponse.CustomersCountResponse> getTotalCustomers() {
        DashboardResponse.CustomersCountResponse response = dashboardService.getTotalCustomers();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<DashboardResponse.MonthlyRevenueResponse> getMonthlyRevenue() {
        DashboardResponse.MonthlyRevenueResponse response = dashboardService.getMonthlyRevenue();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/average-ticket-price")
    public ResponseEntity<DashboardResponse.AverageTicketPriceResponse> getAverageTicketPrice() {
        DashboardResponse.AverageTicketPriceResponse response = dashboardService.getAverageTicketPrice();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/bookings/recent")
    public ResponseEntity<DashboardResponse.RecentBookingsResponse> getRecentBookings() {
        DashboardResponse.RecentBookingsResponse response = dashboardService.getRecentBookings();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/customers/recent")
    public ResponseEntity<DashboardResponse.RecentCustomersResponse> getRecentCustomers() {
        DashboardResponse.RecentCustomersResponse response = dashboardService.getRecentCustomers();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/bookings/target")
    public ResponseEntity<DashboardResponse.BookingTargetResponse> getBookingTarget() {
        DashboardResponse.BookingTargetResponse response = dashboardService.getBookingTarget();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/revenue/target")
    public ResponseEntity<DashboardResponse.RevenueTargetResponse> getRevenueTarget() {
        DashboardResponse.RevenueTargetResponse response = dashboardService.getRevenueTarget();
        return ResponseEntity.status(response.getStatus()).body(response);
    }
} 