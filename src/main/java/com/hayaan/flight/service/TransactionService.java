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

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionService {

    private final TicketHistoryRepo ticketHistoryRepo;
    private final PaymentRepository paymentRepository;
    private final PassengerRepo passengerRepo;

    @Transactional
    public TicketHistory saveTicketHistoryWithPassengers(TicketHistory ticketHistory, List<Passenger> passengers) {
        TicketHistory savedTicketHistory = ticketHistoryRepo.save(ticketHistory);

        for (Passenger passenger : passengers) {
            passenger.setTicketHistory(savedTicketHistory);
            passengerRepo.save(passenger);
        }

        return savedTicketHistory;
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
//                .firstName(ticketHistoryDto.getFirstName())
//                .middleName(ticketHistoryDto.getMiddleName())
//                .lastName(ticketHistoryDto.getLastName())
//                .email(ticketHistoryDto.getEmail())
//                .phoneNumber(ticketHistoryDto.getPhoneNumber())
//                .airlineId(ticketHistoryDto.getAirlineId())
                .airTransactionId(ticketHistoryDto.getAirTransactionId())
                .userId(ticketHistoryDto.getUserId())
//                .userType(ticketHistoryDto.getUserType())
//                .documentIdNumber(ticketHistoryDto.getDocumentIdNumber())
//                .document(ticketHistoryDto.getDocument())
                .status(0) // created
                .createdDate(LocalDateTime.now())
                .expireDate(ticketHistoryDto.getExpireDate())
                .build();

        ticketHistoryRepo.save(ticketHistory);

    }

    public void stagePayment(PaymentStageDto paymentStageDto) {

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

        log.info("PAYMENT RECORD INSERTED : {}");

        paymentRepository.save(payment);

    }

}
