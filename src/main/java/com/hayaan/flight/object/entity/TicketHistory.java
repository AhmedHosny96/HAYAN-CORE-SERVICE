package com.hayaan.flight.object.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TicketHistory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UserId")
    private Long userId;

    @Column(name = "PNR", unique = true)
    private String pnr;

    @Column(name = "Origin")
    private String origin;

    @Column(name = "Destination")
    private String destination;

    @Column(name = "TicketAmount")
    private Double ticketAmount;

    @Column(name = "CommissionAmount")
    private Double commissionAmount;

    @Column(name = "TravelDate")
    private LocalDateTime travelDate;

    @Column(name = "ReturnDate")
    private LocalDate returnDate;

    @Column(name = "AirlineId")
    private String airlineId;
    @Column(name = "AirTransactionId")
    private String airTransactionId;

//    @Column(name = "PaymentDate")
//    private LocalDateTime paymentDate;
//
//    @Column(name = "PaymentMethod")
//    private String paymentMethod;

    @Column(name = "PaymentReference")
    private String paymentReference;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "MiddleName")
    private String middleName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Email")
    private String email;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "UserType")
    private String userType; // C - customer , A - agent , P - partner

    @Column(name = "Document")
    private String document;

    @Column(name = "documentIdNumber")
    private String documentIdNumber;

    @Column(name = "DateOfIssue")
    private LocalDateTime dateOfIssue;

    @Column(name = "ExpiryDate")
    private LocalDateTime expiryDate;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "Status")
    private Integer status; // 0 booking is active payment not made , 1 , booking active payment made , 2 cancelled , 3 cancelled and refunded

    @Column(name = "StatusDesc")
    private String statusDesc;

}
