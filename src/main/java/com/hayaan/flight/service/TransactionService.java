package com.hayaan.flight.service;

import com.hayaan.auth.object.dto.CreateUserDto;
import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.RoleRepo;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.auth.service.UserService;
import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.PaymentStageDto;
import com.hayaan.flight.object.dto.TicketHistoryDto;
import com.hayaan.flight.object.entity.Passenger;
import com.hayaan.flight.object.entity.Payment;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.PassengerRepo;
import com.hayaan.flight.repo.PassengerTicketRepo;
import com.hayaan.flight.repo.PaymentRepository;
import com.hayaan.flight.repo.TicketHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionService {

    private final TicketHistoryRepo ticketHistoryRepo;
    private final PaymentRepository paymentRepository;
    private final PassengerRepo passengerRepo;
    private final PassengerTicketRepo passengerTicketRepo;
    private final UserRepository userRepository;
    private final RoleRepo roleRepo;
    private final UserService userService;

    @Transactional
    public TicketHistory saveTicketHistoryWithPassengers(TicketHistory ticketHistory, List<Passenger> passengers) {

        Optional<TicketHistory> existingTicketHistory = ticketHistoryRepo.findByPnr(ticketHistory.getPnr());
        if (existingTicketHistory.isPresent()) {
            return existingTicketHistory.get();
        }

        TicketHistory savedTicketHistory = ticketHistoryRepo.save(ticketHistory);
        List<PassengerTicket> passengerTickets = new ArrayList<>();

        for (Passenger passengerInput : passengers) {
            Passenger passenger = passengerRepo.findByPhoneNumber(passengerInput.getPhoneNumber())
                    .orElseGet(() -> {
                        passengerInput.setId(null);
                        return passengerRepo.save(passengerInput);
                    });

            PassengerTicket mapping = new PassengerTicket();
            mapping.setPassenger(passenger);
            mapping.setTicketHistory(savedTicketHistory);
            passengerTickets.add(mapping);
        }

        passengerTicketRepo.saveAll(passengerTickets);

        Passenger primary = passengerTickets.get(0).getPassenger();
        User user;

        Optional<User> userOpt = userRepository.findByPhoneNumber(primary.getPhoneNumber());
        if (userOpt.isPresent()) {
            user = userOpt.get();
            log.info("User with phone number {} already exists. Linking to ticket.", primary.getPhoneNumber());
        } else {
            int random4Number = new Random().nextInt(9000) + 1000;
            String username = primary.getFirstName() + random4Number;

            CreateUserDto createUserDto = new CreateUserDto(
                    username,
                    primary.getEmail(),
                    primary.getPhoneNumber(),
                    primary.getFirstName() + primary.getMiddleName() + primary.getLastName(),
                    roleRepo.findByName("CUSTOMER").orElseThrow(() -> new IllegalStateException("Role CUSTOMER not found")).getId(),
                    "SYSTEM"
            );

            CustomResponse userResp = userService.createUser(createUserDto);
            log.info("User created for passenger: {}", userResp.message());

            user = userRepository.findByPhoneNumber(primary.getPhoneNumber())
                    .orElseThrow(() -> new IllegalStateException("User creation succeeded but user not found"));
        }

        // Attach userId to ticketHistory and save
        savedTicketHistory.setUser(user);
        return ticketHistoryRepo.save(savedTicketHistory);
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
