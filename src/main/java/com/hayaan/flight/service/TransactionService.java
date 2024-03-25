package com.hayaan.flight.service;

import com.hayaan.flight.object.dto.PaymentStageDto;
import com.hayaan.flight.object.dto.TicketHistoryDto;
import com.hayaan.flight.object.entity.Payment;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.PaymentRepository;
import com.hayaan.flight.repo.TicketHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionService {

    private final TicketHistoryRepo ticketHistoryRepo;
    private final PaymentRepository paymentRepository;


    // save ticket history
    public void saveTicket(TicketHistoryDto ticketHistoryDto) {

        // save file path;
        TicketHistory ticketHistory = TicketHistory.builder()
                .pnr(ticketHistoryDto.getPnr())
                .origin(ticketHistoryDto.getOrigin())
                .destination(ticketHistoryDto.getDestination())
                .ticketAmount(ticketHistoryDto.getTicketAmount())
                .commissionAmount(ticketHistoryDto.getCommissionAmount())
                .travelDate(ticketHistoryDto.getTravelDate())
                .returnDate(ticketHistoryDto.getReturnDate())
                .firstName(ticketHistoryDto.getFirstName())
                .middleName(ticketHistoryDto.getMiddleName())
                .lastName(ticketHistoryDto.getLastName())
                .email(ticketHistoryDto.getEmail())
                .phoneNumber(ticketHistoryDto.getPhoneNumber())
                .airlineId(ticketHistoryDto.getAirlineId())
                .airTransactionId(ticketHistoryDto.getAirTransactionId())
                .userId(ticketHistoryDto.getUserId())
                .userType(ticketHistoryDto.getUserType())
                .documentIdNumber(ticketHistoryDto.getDocumentIdNumber())
                .document(ticketHistoryDto.getDocument())
                .status(0) // created
                .createdDate(LocalDateTime.now())
                .expiryDate(ticketHistoryDto.getExpireDate())
                .build();

        ticketHistoryRepo.save(ticketHistory);

    }

    public void stagePayment(PaymentStageDto paymentStageDto) {

        Payment payment = Payment.builder()
                .amount(paymentStageDto.getAmount())
                .pnr(paymentStageDto.getPnr())
                .payerAccount(paymentStageDto.getPayerAccount())
                .paymentStatus(0) // pending
                .paymentStatusDesc("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        log.info("PAYMENT RECORD INSERTED : {}");

        paymentRepository.save(payment);

    }

}
