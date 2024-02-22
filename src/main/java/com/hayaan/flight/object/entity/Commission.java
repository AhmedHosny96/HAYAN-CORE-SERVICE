package com.hayaan.flight.object.entity;

import com.hayaan.auth.object.entity.User;
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
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Type", referencedColumnName = "Id")
    private CommissionType commissionTypeId;


    @Column(name = "Value")
    private Double value;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent", referencedColumnName = "Id")
    private User createdBy;
    @Column(name = "Status")
    private Integer status;

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
