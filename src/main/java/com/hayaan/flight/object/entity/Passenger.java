package com.hayaan.flight.object.entity;

import com.hayaan.dto.PassengerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Passenger")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "Document")
    private String document;

    @Column(name = "DocumentIdNumber")
    private String documentIdNumber;

    @Column(name = "DateOfIssue")
    private LocalDateTime dateOfIssue;

    @Column(name = "ExpiryDate")
    private LocalDateTime expiryDate;

    @Column(name = "PassengerType")
    private String passengerType; // A - Adult, C - Child, I - Infant

    @ManyToOne
    @JoinColumn(name = "TicketHistoryId", nullable = false)
    private TicketHistory ticketHistory;
}