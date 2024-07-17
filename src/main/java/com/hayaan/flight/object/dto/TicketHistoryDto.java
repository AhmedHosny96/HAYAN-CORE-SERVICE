package com.hayaan.flight.object.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketHistoryDto {

    private String uniqueId;
    private String pnr;
    private String origin;
    private String destination;
    private Double ticketAmount;
    private Double commissionAmount;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
    private LocalDateTime returnDateTime;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String airlineId;
    private String flightNumber;
    private Long userId;
    private int totalNoOfPassengers;
    private String userType;
    private String document;
    private String documentIdNumber;
    private String airTransactionId;
    private LocalDateTime expireDate;

}
