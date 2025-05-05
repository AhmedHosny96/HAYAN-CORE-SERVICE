package com.hayaan.flight.object.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hayaan.auth.object.entity.User;
import com.hayaan.flight.object.FlightType;
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
@Entity(name = "Commission")
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CreatedById", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "commissionTypeId", nullable = false)
    private CommissionType commissionType;

    @Column(name = "UserType")
    private String UserType;

    @Column(name = "Amount")
    private Double amount;


    @Column(name = "Currency")
    private String currency;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "FlightType") // domestic iyo internation
    @Enumerated(EnumType.STRING)
    private FlightType flightType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Temporal(TemporalType.TIMESTAMP)
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
//2.COMMISION TABLE
//
//        1.id
//        2.commisionTypeId/percentage/fixed
//        3.agentId
//        4.partnerId
//        5.value //
//        6.status
//        7.createdBy // user
//        8.createdAt
//        9.updatedAt
