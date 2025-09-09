package com.hayaan.service;

import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.dto.DashboardResponse;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.TicketHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final TicketHistoryRepo ticketHistoryRepo;
    private final UserRepository userRepository;

    public DashboardResponse.BookingsCountResponse getTodayBookings() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            List<TicketHistory> todayBookings = ticketHistoryRepo.findAllByCreatedDateBetween(startOfDay, endOfDay);

            return DashboardResponse.BookingsCountResponse.builder()
                    .status(200)
                    .message("Today's bookings retrieved successfully")
                    .todayBookings(todayBookings.size())
                    .build();

        } catch (Exception e) {
            log.error("Error getting today's bookings", e);
            return DashboardResponse.BookingsCountResponse.builder()
                    .status(500)
                    .message("Error retrieving today's bookings")
                    .build();
        }
    }

    public DashboardResponse.CustomersCountResponse getTotalCustomers() {
        try {
            long totalCustomers = userRepository.count();

            return DashboardResponse.CustomersCountResponse.builder()
                    .status(200)
                    .message("Total customers retrieved successfully")
                    .totalCustomers((int) totalCustomers)
                    .build();

        } catch (Exception e) {
            log.error("Error getting total customers", e);
            return DashboardResponse.CustomersCountResponse.builder()
                    .status(500)
                    .message("Error retrieving total customers")
                    .build();
        }
    }

    public DashboardResponse.MonthlyRevenueResponse getMonthlyRevenue() {
        try {
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

            List<TicketHistory> monthlyBookings = ticketHistoryRepo.findAllByCreatedDateBetween(startOfMonth, endOfMonth);

            // Filter paid bookings (status = 1)
            List<TicketHistory> paidBookings = monthlyBookings.stream()
                    .filter(booking -> booking.getStatus() != null && booking.getStatus() == 1)
                    .filter(booking -> booking.getTotalAmount() != null && booking.getTotalAmount() > 0)
                    .filter(booking -> booking.getCurrency() != null)
                    .collect(Collectors.toList());

            // Group by currency for total revenue
            Map<String, Double> revenueByCurrency = paidBookings.stream()
                    .collect(Collectors.groupingBy(
                            booking -> booking.getCurrency() != null ? booking.getCurrency() : "USD",
                            Collectors.summingDouble(booking -> booking.getTotalAmount())
                    ));

            List<DashboardResponse.CurrencyRevenue> currencyRevenueList = revenueByCurrency.entrySet().stream()
                    .map(entry -> DashboardResponse.CurrencyRevenue.builder()
                            .currency(entry.getKey().trim())
                            .totalRevenue(entry.getValue())
                            .build())
                    .collect(Collectors.toList());

            // Group by day and currency for daily breakdown
            Map<String, Map<LocalDate, Double>> dailyRevenueByCurrency = paidBookings.stream()
                    .collect(Collectors.groupingBy(
                            booking -> booking.getCurrency() != null ? booking.getCurrency() : "USD",
                            Collectors.groupingBy(
                                    booking -> booking.getCreatedDate().toLocalDate(),
                                    Collectors.summingDouble(TicketHistory::getTotalAmount)
                            )
                    ));

            List<DashboardResponse.DailyRevenue> dailyBreakdown = new ArrayList<>();
            for (Map.Entry<String, Map<LocalDate, Double>> currencyEntry : dailyRevenueByCurrency.entrySet()) {
                String currency = currencyEntry.getKey();
                for (Map.Entry<LocalDate, Double> dailyEntry : currencyEntry.getValue().entrySet()) {
                    dailyBreakdown.add(DashboardResponse.DailyRevenue.builder()
                            .date(dailyEntry.getKey())
                            .currency(currency)
                            .revenue(dailyEntry.getValue())
                            .build());
                }
            }

            dailyBreakdown.sort((a, b) -> {
                int dateComparison = a.getDate().compareTo(b.getDate());
                return dateComparison != 0 ? dateComparison : a.getCurrency().compareTo(b.getCurrency());
            });

            return DashboardResponse.MonthlyRevenueResponse.builder()
                    .status(200)
                    .message("Monthly revenue retrieved successfully")
                    .revenueByCurrency(currencyRevenueList)
                    .dailyBreakdown(dailyBreakdown)
                    .build();

        } catch (Exception e) {
            log.error("Error getting monthly revenue", e);
            return DashboardResponse.MonthlyRevenueResponse.builder()
                    .status(500)
                    .message("Error retrieving monthly revenue")
                    .build();
        }
    }

    public DashboardResponse.AverageTicketPriceResponse getAverageTicketPrice() {
        try {
            List<TicketHistory> allBookings = ticketHistoryRepo.findAll();

            // Filter paid bookings (status = 1)
            List<TicketHistory> paidBookings = allBookings.stream()
                    .filter(booking -> booking.getStatus() != null && booking.getStatus() == 1)
                    .filter(booking -> booking.getTotalAmount() != null && booking.getTotalAmount() > 0)
                    .filter(booking -> booking.getCurrency() != null)
                    .collect(Collectors.toList());

            if (paidBookings.isEmpty()) {
                return DashboardResponse.AverageTicketPriceResponse.builder()
                        .status(200)
                        .message("No ticket data available")
                        .averagePriceByCurrency(new ArrayList<>())
                        .build();
            }

            // Group by currency and calculate average
            Map<String, List<TicketHistory>> bookingsByCurrency = paidBookings.stream()
                    .collect(Collectors.groupingBy(booking -> booking.getCurrency()));

            List<DashboardResponse.CurrencyAveragePrice> averagePriceList = bookingsByCurrency.entrySet().stream()
                    .map(entry -> {
                        String currency = entry.getKey();
                        List<TicketHistory> currencyBookings = entry.getValue();

                        Double averagePrice = currencyBookings.stream()
                                .mapToDouble(TicketHistory::getTotalAmount)
                                .average()
                                .orElse(0.0);

                        return DashboardResponse.CurrencyAveragePrice.builder()
                                .currency(currency)
                                .averagePrice(averagePrice)
                                .totalTickets(currencyBookings.size())
                                .build();
                    })
                    .collect(Collectors.toList());

            return DashboardResponse.AverageTicketPriceResponse.builder()
                    .status(200)
                    .message("Average ticket price retrieved successfully")
                    .averagePriceByCurrency(averagePriceList)
                    .build();

        } catch (Exception e) {
            log.error("Error getting average ticket price", e);
            return DashboardResponse.AverageTicketPriceResponse.builder()
                    .status(500)
                    .message("Error retrieving average ticket price")
                    .build();
        }
    }

    public DashboardResponse.RecentBookingsResponse getRecentBookings() {
        try {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
            List<TicketHistory> allRecentBookings = ticketHistoryRepo.findAll(pageable).getContent();

            // Filter only successful bookings (status = 1)
            List<TicketHistory> recentSuccessfulBookings = allRecentBookings.stream()
                    .filter(booking -> booking.getStatus() != null && booking.getStatus() == 1)
                    .limit(10)
                    .collect(Collectors.toList());

            return DashboardResponse.RecentBookingsResponse.builder()
                    .status(200)
                    .message("Recent successful bookings retrieved successfully")
                    .recentBookings(recentSuccessfulBookings)
                    .build();

        } catch (Exception e) {
            log.error("Error getting recent bookings", e);
            return DashboardResponse.RecentBookingsResponse.builder()
                    .status(500)
                    .message("Error retrieving recent bookings")
                    .build();
        }
    }

    public DashboardResponse.RecentCustomersResponse getRecentCustomers() {
        try {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
            List<User> recentCustomers = userRepository.findAll(pageable).getContent();

            return DashboardResponse.RecentCustomersResponse.builder()
                    .status(200)
                    .message("Recent customers retrieved successfully")
                    .recentCustomers(recentCustomers)
                    .build();

        } catch (Exception e) {
            log.error("Error getting recent customers", e);
            return DashboardResponse.RecentCustomersResponse.builder()
                    .status(500)
                    .message("Error retrieving recent customers")
                    .build();
        }
    }

    public DashboardResponse.BookingTargetResponse getBookingTarget() {
        try {
            // For demo purposes, setting a static target. In real implementation, 
            // this would come from a configuration or database
            int targetBookings = 100;

            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

            List<TicketHistory> monthlyBookings = ticketHistoryRepo.findAllByCreatedDateBetween(startOfMonth, endOfMonth);
            int actualBookings = monthlyBookings.size();

            double achievementPercentage = targetBookings > 0 ? (double) actualBookings / targetBookings * 100 : 0;

            return DashboardResponse.BookingTargetResponse.builder()
                    .status(200)
                    .message("Booking target retrieved successfully")
                    .targetBookings(targetBookings)
                    .actualBookings(actualBookings)
                    .achievementPercentage(achievementPercentage)
                    .build();

        } catch (Exception e) {
            log.error("Error getting booking target", e);
            return DashboardResponse.BookingTargetResponse.builder()
                    .status(500)
                    .message("Error retrieving booking target")
                    .build();
        }
    }

    public DashboardResponse.RevenueTargetResponse getRevenueTarget() {
        try {
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

            List<TicketHistory> monthlyBookings = ticketHistoryRepo.findAllByCreatedDateBetween(startOfMonth, endOfMonth);

            // Filter paid bookings (status = 1)
            List<TicketHistory> paidBookings = monthlyBookings.stream()
                    .filter(booking -> booking.getStatus() != null && booking.getStatus() == 1)
                    .filter(booking -> booking.getTotalAmount() != null && booking.getTotalAmount() > 0)
                    .filter(booking -> booking.getCurrency() != null)
                    .collect(Collectors.toList());

            // Group by currency for actual revenue
            Map<String, Double> actualRevenueByCurrency = paidBookings.stream()
                    .collect(Collectors.groupingBy(
                            TicketHistory::getCurrency,
                            Collectors.summingDouble(TicketHistory::getTotalAmount)
                    ));

            // For demo purposes, setting static targets per currency. 
            // In real implementation, this would come from a configuration or database
            Map<String, Double> targetRevenueByCurrency = Map.of(
                    "USD", 50000.0,
                    "EUR", 40000.0,
                    "GBP", 35000.0
            );

            List<DashboardResponse.CurrencyRevenueTarget> targetList = new ArrayList<>();

            // Add targets for currencies that have actual revenue
            for (String currency : actualRevenueByCurrency.keySet()) {
                Double targetRevenue = targetRevenueByCurrency.getOrDefault(currency, 10000.0); // Default target
                Double actualRevenue = actualRevenueByCurrency.get(currency);
                Double achievementPercentage = targetRevenue > 0 ? actualRevenue / targetRevenue * 100 : 0;

                targetList.add(DashboardResponse.CurrencyRevenueTarget.builder()
                        .currency(currency)
                        .targetRevenue(targetRevenue)
                        .actualRevenue(actualRevenue)
                        .achievementPercentage(achievementPercentage)
                        .build());
            }

            // Add targets for currencies with no actual revenue
            for (Map.Entry<String, Double> targetEntry : targetRevenueByCurrency.entrySet()) {
                String currency = targetEntry.getKey();
                if (!actualRevenueByCurrency.containsKey(currency)) {
                    targetList.add(DashboardResponse.CurrencyRevenueTarget.builder()
                            .currency(currency)
                            .targetRevenue(targetEntry.getValue())
                            .actualRevenue(0.0)
                            .achievementPercentage(0.0)
                            .build());
                }
            }

            return DashboardResponse.RevenueTargetResponse.builder()
                    .status(200)
                    .message("Revenue target retrieved successfully")
                    .targetByCurrency(targetList)
                    .build();

        } catch (Exception e) {
            log.error("Error getting revenue target", e);
            return DashboardResponse.RevenueTargetResponse.builder()
                    .status(500)
                    .message("Error retrieving revenue target")
                    .build();
        }
    }
} 