package com.hayaan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hayaan.auth.object.entity.User;
import com.hayaan.flight.object.entity.TicketHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BookingsCountResponse {
        private int status;
        private String message;
        private Integer todayBookings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomersCountResponse {
        private int status;
        private String message;
        private Integer totalCustomers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MonthlyRevenueResponse {
        private int status;
        private String message;
        private List<CurrencyRevenue> revenueByCurrency;
        private List<DailyRevenue> dailyBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRevenue {
        private LocalDate date;
        private String currency;
        private Double revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyRevenue {
        private String currency;
        private Double totalRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AverageTicketPriceResponse {
        private int status;
        private String message;
        private List<CurrencyAveragePrice> averagePriceByCurrency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyAveragePrice {
        private String currency;
        private Double averagePrice;
        private Integer totalTickets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecentBookingsResponse {
        private int status;
        private String message;
        private List<TicketHistory> recentBookings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecentCustomersResponse {
        private int status;
        private String message;
        private List<User> recentCustomers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BookingTargetResponse {
        private int status;
        private String message;
        private Integer targetBookings;
        private Integer actualBookings;
        private Double achievementPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RevenueTargetResponse {
        private int status;
        private String message;
        private List<CurrencyRevenueTarget> targetByCurrency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyRevenueTarget {
        private String currency;
        private Double targetRevenue;
        private Double actualRevenue;
        private Double achievementPercentage;
    }
} 