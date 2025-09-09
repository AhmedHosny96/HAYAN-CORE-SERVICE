package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.entity.TicketHistory;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookingReportResp {

    private int status;
    private String message;
    private List<TicketHistory> bookingHistory;
    private int totalTickets;
    private int totalFailedTickets;;
    private int totalOnProcessTickets;
    private int totalPendingTickets;
    private int totalCancelledTickets;
    private int totalConfirmedTickets;
}
