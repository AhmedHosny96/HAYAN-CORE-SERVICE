
package com.hayaan.flight.object.entity;

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

    @Column(name = "UserId")
    private Long userId;

    @Column(name = "PNR", unique = true)
    private String pnr;

    @Column(name = "UniqueId")
    private String uniqueId;

    @Column(name = "Origin")
    private String origin;

    @Column(name = "Destination")
    private String destination;

    @Column(name = "TicketAmount")
    private Double ticketAmount;

    @Column(name = "CommissionAmount")
    private Double commissionAmount;

    @Column(name = "DepartureDateTime")
    private LocalDateTime departureDateTime;

    @Column(name = "ArrivalDateTime")
    private LocalDateTime arrivalDateTime;

    @Column(name = "ReturnDateTime")
    private LocalDateTime returnDateTime;

    @Column(name = "GoflightNumber")
    private String goflightNumber; // airlinecode + flight number

    @Column(name = "ReturnFlightNumber")
    private String returnFlightNumber; // airlinecode + flight number


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
    private List<Passenger> passengers;

}


//package com.hayaan.flight.object.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "TicketHistory")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class TicketHistory {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "UserId")
//    private Long userId;
//
//    @Column(name = "PNR", unique = true)
//    private String pnr;
//
//    @Column(name = "UniqueId")
//    private String uniqueId;
//
//    @Column(name = "Origin")
//    private String origin;
//
//    @Column(name = "Destination")
//    private String destination;
//
//    @Column(name = "TicketAmount")
//    private Double ticketAmount;
//
//    @Column(name = "CommissionAmount")
//    private Double commissionAmount;
//
//    @Column(name = "TravelDate")
//    private LocalDateTime travelDate;
//
//    @Column(name = "ReturnDate")
//    private LocalDate returnDate;
//
//    @Column(name = "AirlineId")
//    private String airlineId;
//    @Column(name = "AirTransactionId")
//    private String airTransactionId;
//
////    @Column(name = "PaymentDate")
////    private LocalDateTime paymentDate;
////
////    @Column(name = "PaymentMethod")
////    private String paymentMethod;
//
//    @Column(name = "PaymentReference")
//    private String paymentReference;
//
//    @Column(name = "FirstName")
//    private String firstName;
//
//    @Column(name = "MiddleName")
//    private String middleName;
//
//    @Column(name = "LastName")
//    private String lastName;
//
//    @Column(name = "Email")
//    private String email;
//
//    @Column(name = "PhoneNumber")
//    private String phoneNumber;
//
//    @Column(name = "UserType")
//    private String userType; // C - customer , A - agent , P - partner
//
//    @Column(name = "Document")
//    private String document;
//
//    @Column(name = "documentIdNumber")
//    private String documentIdNumber;
//
//    @Column(name = "DateOfIssue")
//    private LocalDateTime dateOfIssue;
//
//    @Column(name = "ExpiryDate")
//    private LocalDateTime expiryDate;
//
//    @Column(name = "CreatedDate")
//    private LocalDateTime createdDate;
//
//    @Column(name = "Status")
//    private Integer status; // 0 booking is active payment not made , 1 , booking active payment made , 2 cancelled , 3 cancelled and refunded
//
//    @Column(name = "StatusDesc")
//    private String statusDesc;
//
//}
