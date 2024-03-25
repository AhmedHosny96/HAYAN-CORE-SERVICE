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
@Entity(name = "Payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Pnr")
    private String pnr;

    @Column(name = "PayerAccount")
    private String payerAccount;

    @Column(name = "Amount")
    private Double amount;
    @Column(name = "PaymentMode")

    private String paymentMode;
    @Column(name = "PaymentReference")

    private String paymentReference;
    @Column(name = "PaymentDate")

    private LocalDateTime paymentDate;
    @Column(name = "RequestBody")

    private String requestBody;
    @Column(name = "ResponseBody")

    private String responseBody;

    @Column(name = "Response")

    private String response;

    @Column(name = "UserType")
    private String userType;

    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "PaymentStatus")
    private Integer paymentStatus;

    @Column(name = "PaymentStatusDesc")
    private String paymentStatusDesc;

    @Column(name = "CreatedAt")

    private LocalDateTime createdAt;
    @Column(name = "UpdatedAt")

    private LocalDateTime updatedAt;
}
