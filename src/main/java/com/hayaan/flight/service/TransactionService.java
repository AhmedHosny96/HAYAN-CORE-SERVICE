package com.hayaan.flight.service;

import com.hayaan.flight.object.dto.PaymentStageDto;
import com.hayaan.flight.object.dto.TicketHistoryDto;
import com.hayaan.flight.object.entity.Passenger;
import com.hayaan.flight.object.entity.Payment;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.PassengerRepo;
import com.hayaan.flight.repo.PaymentRepository;
import com.hayaan.flight.repo.TicketHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionService {

    private final TicketHistoryRepo ticketHistoryRepo;
    private final PaymentRepository paymentRepository;
    private final PassengerRepo passengerRepo;

    @Transactional
    public TicketHistory saveTicketHistoryWithPassengers(TicketHistory ticketHistory, List<Passenger> passengers) {
        // Check if a ticket with the same PNR already exists
        Optional<TicketHistory> existingTicketHistory = ticketHistoryRepo.findByPnr(ticketHistory.getPnr());

        if (existingTicketHistory.isPresent()) {
            // If the ticket already exists, return the existing ticket history
            return existingTicketHistory.get();
        }

        // Save the new ticket history if it doesn't exist
        TicketHistory savedTicketHistory = ticketHistoryRepo.save(ticketHistory);

        // Save the associated passengers
        for (Passenger passenger : passengers) {
            passenger.setTicketHistory(savedTicketHistory);
            passengerRepo.save(passenger);
        }

        return savedTicketHistory; // Return the newly saved ticket history
    }


    // save ticket history
    public void saveTicket(TicketHistoryDto ticketHistoryDto) {

        // save file path;
        TicketHistory ticketHistory = TicketHistory.builder()
                .pnr(ticketHistoryDto.getPnr())
                .origin(ticketHistoryDto.getOrigin())
                .destination(ticketHistoryDto.getDestination())
                .ticketAmount(ticketHistoryDto.getTicketAmount())
                .commissionAmount(ticketHistoryDto.getCommissionAmount())
                .departureDateTime(ticketHistoryDto.getDepartureDateTime())
                .returnDateTime(ticketHistoryDto.getReturnDateTime())

                .airTransactionId(ticketHistoryDto.getAirTransactionId())
                .user(ticketHistoryDto.getUser())
                .agent(ticketHistoryDto.getAgent())
                .status(0) // created
                .createdDate(LocalDateTime.now())
                .expireDate(ticketHistoryDto.getExpireDate())
                .build();

        ticketHistoryRepo.save(ticketHistory);

    }

    public void stagePayment(PaymentStageDto paymentStageDto) {
        // Check if a payment with the same PNR already exists
        boolean exists = paymentRepository.existsByPnr(paymentStageDto.getPnr());
        if (exists) {
            log.warn("Duplicate PNR detected: {}. Skipping insert.", paymentStageDto.getPnr());
            return;
        }

        Payment payment = Payment.builder()
                .amount(paymentStageDto.getAmount())
                .pnr(paymentStageDto.getPnr())
                .payerAccount(paymentStageDto.getPayerAccount())
                .paymentStatus(0) // pending
                .pendingNotification(false)
                .reminderNotification(false)
                .successPaymentNotification(false)
                .withOutPaymentNotification(false)
                .paymentStatusDesc("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
        log.info("PAYMENT RECORD INSERTED : {}", payment.getPnr());
    }


}
