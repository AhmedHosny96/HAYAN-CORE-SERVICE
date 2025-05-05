package com.hayaan.flight.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardingDto {

    private String name;
    private String flight;
    private String seat;
    private String from;
    private String to;
    private String departureTime;
    private String arrivalTime;
    private String pnr;
    private String eTicketNumber;
    private String outputPath;
    private String ticketStatus;
}
