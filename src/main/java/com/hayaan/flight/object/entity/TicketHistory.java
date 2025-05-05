
package com.hayaan.flight.object.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hayaan.auth.object.entity.User;
import com.hayaan.dto.TravelType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentId")
    @JsonBackReference // Prevents nesting agent → ticketHistory → agent...
    private Agent agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId")
    @JsonBackReference
    private User user;

    @Column(name = "PNR", unique = true)
    private String pnr;

    @Column(name = "UniqueId")
    private String uniqueId;

    @Column(name = "PtrUniqueID") //pending ticket reissue uniqueId
    private Long ptrUniqueID;

    @Column(name = "EticketNumber")
    private String eticketNumber;

    @Column(name = "Origin")
    private String origin;

    @Column(name = "Destination")
    private String destination;

    @Column(name = "TicketAmount")
    private Double ticketAmount;

    @Column(name = "CommissionAmount")
    private Double commissionAmount;

    @Column(name = "TotalAmount")
    private Double totalAmount;

    @Column(name = "Currency")
    private String currency;

    @Column(name = "DepartureDateTime")
    private LocalDateTime departureDateTime;

    @Column(name = "ArrivalDateTime")
    private LocalDateTime arrivalDateTime;

    @Column(name = "ReturnDateTime")
    private LocalDateTime returnDateTime;

    @Column(name = "FlightNumber")
    private String flightNumber; // airlinecode + flight number

    @Column(name = "AirTransactionId")
    private String airTransactionId;

    @Column(name = "TotalNoOfPassengers")
    private int totalNoOfPassengers;

    @Column(name = "PaymentReference")
    private String paymentReference;

    @Column(name = "FlightType")
    private String flightType; // two way or one way

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "ExpireDate")
    private LocalDateTime expireDate;

    @Column(name = "Status")
    private Integer status; // 0 - booking is active, payment not made; 1 - booking active, payment made; 2 - cancelled; 3 - cancelled and refunded

    @Column(name = "StatusDesc")
    private String statusDesc;

    @OneToMany(mappedBy = "ticketHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Passenger> passengers;

}