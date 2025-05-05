package com.hayaan.flight.service;


import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.flight.object.dto.BookingByUsers;
import com.hayaan.flight.object.entity.Agent;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.AgentRepo;
import com.hayaan.flight.repo.TicketHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final TicketHistoryRepo ticketHistoryRepo;
    private final AgentRepo agentRepo;
    private final UserRepository userRepository;

    // get All Bookings by agentId

    public BookingByUsers getBookingsByAgent(Long agentId) {


        Agent agent = agentRepo.findById(agentId).orElse(null);


        List<TicketHistory> bookings = ticketHistoryRepo.findByAgent(agent);

        log.info("bookings by agent : {}", bookings);

        int totalTickets = bookings.size();
        int totalPendingTickets = 0;
        int totalConfirmedTickets = 0;
        int totalCancelledTickets = 0;
        int totalFailedTickets = 0;

        for (TicketHistory ticket : bookings) {
            if (ticket.getStatus() != null) {
                switch (ticket.getStatus()) {
                    case 0:
                        totalPendingTickets++;
                        break;
                    case 1:
                        totalConfirmedTickets++;
                        break;
                    case 2:
                        totalCancelledTickets++;
                        break;
                    case 3:
                        totalFailedTickets++;
                        break;
                    default:
                        // Unknown status, you can handle if needed
                        break;
                }
            }
        }

        return BookingByUsers.builder()
                .status(200)
                .message("Success")
                .bookingHistory(bookings) // We are returning only the summary, not a specific booking here
                .totalTickets(totalTickets)
                .totalPendingTickets(totalPendingTickets)
                .totalConfirmedTickets(totalConfirmedTickets)
                .totalCancelledTickets(totalCancelledTickets)
                .totalFailedTickets(totalFailedTickets)
                .build();
    }

    public BookingByUsers getBookingsByUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        List<TicketHistory> bookings = ticketHistoryRepo.findByUser(user);

        log.info("bookings by user: {}", bookings);

        int totalTickets = bookings.size();
        int totalPendingTickets = 0;
        int totalConfirmedTickets = 0;
        int totalCancelledTickets = 0;
        int totalFailedTickets = 0;

        for (TicketHistory ticket : bookings) {
            if (ticket.getStatus() != null) {
                switch (ticket.getStatus()) {
                    case 0:
                        totalPendingTickets++;
                        break;
                    case 1:
                        totalConfirmedTickets++;
                        break;
                    case 2:
                        totalCancelledTickets++;
                        break;
                    case 3:
                        totalFailedTickets++;
                        break;
                    default:
                        // Unknown status, you can handle if needed
                        break;
                }
            }
        }

        return BookingByUsers.builder()
                .status(200)
                .message("Success")
                .bookingHistory(bookings) // We are returning only the summary, not a specific booking here
                .totalTickets(totalTickets)
                .totalPendingTickets(totalPendingTickets)
                .totalConfirmedTickets(totalConfirmedTickets)
                .totalCancelledTickets(totalCancelledTickets)
                .totalFailedTickets(totalFailedTickets)
                .build();
    }

    public BookingByUsers getBookingsByPassenger(Long passengerId) {
        List<TicketHistory> bookings = ticketHistoryRepo.findByPassengerId(passengerId);

        if (bookings.isEmpty()) {
            return BookingByUsers.builder()
                    .status(404)
                    .message("No bookings found for passenger ID: " + passengerId)
                    .build();
        }

        int totalTickets = bookings.size();
        int totalPendingTickets = 0;
        int totalConfirmedTickets = 0;
        int totalCancelledTickets = 0;
        int totalFailedTickets = 0;

        for (TicketHistory ticket : bookings) {
            if (ticket.getStatus() != null) {
                switch (ticket.getStatus()) {
                    case 0 -> totalPendingTickets++;
                    case 1 -> totalConfirmedTickets++;
                    case 2 -> totalCancelledTickets++;
                    case 3 -> totalFailedTickets++;
                }
            }
        }

        return BookingByUsers.builder()
                .status(200)
                .message("Success")
                .bookingHistory(bookings)
                .totalTickets(totalTickets)
                .totalPendingTickets(totalPendingTickets)
                .totalConfirmedTickets(totalConfirmedTickets)
                .totalCancelledTickets(totalCancelledTickets)
                .totalFailedTickets(totalFailedTickets)
                .build();
    }



}
