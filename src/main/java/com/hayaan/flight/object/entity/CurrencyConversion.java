package com.hayaan.flight.object.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CurrencyConversion", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"BaseCurrency", "TargetCurrency", "Date"})
})
public class CurrencyConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "BaseCurrency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "TargetCurrency", nullable = false, length = 3)
    private String targetCurrency;

    @Column(name = "Rate", nullable = false)
    private Double rate;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "Source")
    private String source;

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
