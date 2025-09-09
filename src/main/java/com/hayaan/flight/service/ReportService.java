package com.hayaan.flight.service;


import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.flight.object.dto.BookingReportResp;
import com.hayaan.flight.object.entity.Agent;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.AgentRepo;
import com.hayaan.flight.repo.TicketHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final TicketHistoryRepo ticketHistoryRepo;
    private final AgentRepo agentRepo;
    private final UserRepository userRepository;

    // get All Bookings by agentId

    public BookingReportResp getBookingsByAgent(Long agentId) {


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

        return BookingReportResp.builder()
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

    public BookingReportResp getBookingsByUser(Long userId) {
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

        return BookingReportResp.builder()
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

    public BookingReportResp getBookingsByPassenger(Long passengerId) {

        Optional<User> userRepositoryById = userRepository.findById(passengerId);

        if (!userRepositoryById.isPresent()) {
            return BookingReportResp.builder()
                    .status(400)
                    .message("Invalid user ID")
                    .build();
        }

        User user = userRepositoryById.get();

        List<TicketHistory> bookings = ticketHistoryRepo.findByUser(user);

        if (bookings.isEmpty()) {
            return BookingReportResp.builder()
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

        return BookingReportResp.builder()
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


    public BookingReportResp getAllBookingsByDate(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startOfDay = startDate.atStartOfDay();
        LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);

        List<TicketHistory> bookings = ticketHistoryRepo.findAllByCreatedDateBetween(startOfDay, endOfDay);

        int total = bookings.size();
        int totalPending = 0;
        int totalSuccess = 0;
        int totalOnProcess = 0;
        int totalCancelled = 0;
        int totalReissued = 0;

        for (TicketHistory booking : bookings) {
            if (booking.getStatus() == null) continue;
            switch (booking.getStatus()) {
                case 0 -> totalPending++;
                case 1 -> totalSuccess++;
                case 2 -> totalOnProcess++;
                case 3 -> totalCancelled++;
                case 5 -> totalReissued++;
            }
        }

        return BookingReportResp.builder()
                .status(200)
                .message("success")
                .bookingHistory(bookings)
                .totalTickets(total)
                .totalPendingTickets(totalPending)
                .totalConfirmedTickets(totalSuccess)
                .totalCancelledTickets(totalCancelled)
                .totalFailedTickets(totalReissued)
                .totalOnProcessTickets(totalOnProcess)
                .build();
    }




}
