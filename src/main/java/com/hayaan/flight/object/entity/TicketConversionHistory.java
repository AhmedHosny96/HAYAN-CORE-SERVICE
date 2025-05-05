package com.hayaan.flight.object.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "TicketConversionHistory")
public class TicketConversionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "BookingReference", nullable = false, length = 3)
    private String bookingReference;

    @Column(name = "BaseCurrency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "TargetCurrency", nullable = false, length = 3)
    private String targetCurrency;

    @Column(name = "Rate", nullable = false)
    private Double rate;

    @Column(name = "OriginalAmount")
    private Double originalAmount;

    @Column(name = "AmountAfter")
    private Double amountAfter;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}
