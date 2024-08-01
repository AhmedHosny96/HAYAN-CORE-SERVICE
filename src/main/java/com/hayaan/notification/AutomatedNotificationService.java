package com.hayaan.notification;


import com.hayaan.flight.object.entity.Passenger;
import com.hayaan.flight.object.entity.Payment;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.PassengerRepo;
import com.hayaan.flight.repo.PaymentRepository;
import com.hayaan.flight.repo.TicketHistoryRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
@Slf4j
public class AutomatedNotificationService {


    private final NotificationService notificationService;

    private final PaymentRepository paymentRepository;

    private final TicketHistoryRepo ticketHistoryRepo;

    private final PassengerRepo passengerRepo;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    // success payment notification
    @Scheduled(fixedRate = 60000)
    public void successPaymentNotification() {

        Optional<List<Payment>> byPaymentStatus = paymentRepository.findByPaymentStatusAndSuccessPaymentNotification(2, false);

        if (!byPaymentStatus.isPresent() || byPaymentStatus.get().isEmpty()) {
            log.info("No success payment");
            return;
        }

        List<Payment> pendingPayments = byPaymentStatus.get();

        for (Payment payment : pendingPayments) {

            Optional<TicketHistory> ticketByPnr = ticketHistoryRepo.findByPnr(payment.getPnr());

            if (ticketByPnr.isPresent()) {
                TicketHistory ticketHistory = ticketByPnr.get();

                String formattedArrivalDate = ticketHistory.getArrivalDateTime().format(DATE_TIME_FORMATTER);
                ;
                String formattedDepartureDate = ticketHistory.getDepartureDateTime().format(DATE_TIME_FORMATTER);
                // query passenger
                Optional<List<Passenger>> passengerList = passengerRepo.findByTicketHistoryId(ticketHistory.getId());

                if (passengerList.isPresent() && !passengerList.get().isEmpty()) {
                    List<Passenger> passengers = passengerList.get();

                    Passenger passenger = passengers.get(0);
                    String email = passenger.getEmail();
                    String customerName = passenger.getFirstName() + passenger.getLastName();

                    Context context = new Context();
                    context.setVariable("pnr", ticketHistory.getPnr());
                    context.setVariable("customerName", customerName);
                    context.setVariable("customerEmail", email);
                    context.setVariable("noOfPassengers", ticketHistory.getTotalNoOfPassengers());
                    context.setVariable("origin", ticketHistory.getOrigin());
                    context.setVariable("departureDateAndTime", formattedDepartureDate);
                    context.setVariable("destination", ticketHistory.getDestination());
                    context.setVariable("arrivalDateAndTime", formattedArrivalDate);
                    context.setVariable("flightNumber", ticketHistory.getFlightNumber());
                    context.setVariable("class", "ECONOMY");
                    context.setVariable("ticketAmount", ticketHistory.getTicketAmount());

                    // email
                    CompletableFuture<Void> completableFuture = notificationService.sendMail(email, "Success payment notification", "success-payment", context);

                    completableFuture.thenRun(() -> {
                        payment.setSuccessPaymentNotification(true);
                        paymentRepository.save(payment);
                        log.info("Notification sent and payment updated for PNR: {}", payment.getPnr());
                    }).exceptionally(ex -> {
                        log.error("Failed to send notification for PNR: {}", payment.getPnr(), ex);
                        return null;
                    });
                    // sms
                    String messageTemplate = "Dear %s. Payment received! Your Hayaan Travel booking is confirmed. Booking Ref: %s. Check your email for details.";

                    String formattedMessage = String.format(messageTemplate, passenger.getFirstName(), ticketHistory.getPnr());

                    notificationService.sendSms(passenger.getPhoneNumber(),
                            formattedMessage);

                } else {
                    log.warn("No passengers found for ticket history ID: {}", ticketHistory.getId());
                }
            } else {
                log.warn("No ticket history found for PNR: {}", payment.getPnr());
            }


        }
    }

    // 24 hours before flight
    @Scheduled(fixedRate = 60000)
    public void twentyFourHourPriorReminder() {

        log.info("LOOKING UP PENDING PAYMENTS : {}");

        Optional<List<Payment>> byPaymentStatus = paymentRepository.findPaymentByPaymentStatusAndReminderNotification(2, false);

        if (!byPaymentStatus.isPresent() || byPaymentStatus.get().isEmpty()) {
            log.info("No pending payments");
            return;
        }

        List<Payment> pendingPayments = byPaymentStatus.get();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyFourHoursLater = now.plusHours(24);

        for (Payment payment : pendingPayments) {

            LocalDateTime paymentCreatedAt = payment.getCreatedAt();

            if (paymentCreatedAt.isBefore(twentyFourHoursLater) && paymentCreatedAt.toLocalDate().isEqual(now.toLocalDate())) {


                Optional<TicketHistory> ticketByPnr = ticketHistoryRepo.findByPnr(payment.getPnr());

                if (ticketByPnr.isPresent()) {
                    TicketHistory ticketHistory = ticketByPnr.get();

                    // Check if the departure date and time is within the next 24 hours
                    LocalDateTime departureDateTime = ticketHistory.getDepartureDateTime();
                    if (departureDateTime.isAfter(now) && departureDateTime.isBefore(twentyFourHoursLater)) {

                        String formattedArrivalDate = ticketHistory.getArrivalDateTime().format(DATE_TIME_FORMATTER);
                        String formattedDepartureDate = departureDateTime.format(DATE_TIME_FORMATTER);

                        // query passenger
                        Optional<List<Passenger>> passengerList = passengerRepo.findByTicketHistoryId(ticketHistory.getId());

                        if (passengerList.isPresent() && !passengerList.get().isEmpty()) {
                            List<Passenger> passengers = passengerList.get();

                            Passenger passenger = passengers.get(0);
                            String email = passenger.getEmail();
                            String customerName = passenger.getFirstName() + " " + passenger.getLastName();

                            Context context = new Context();
                            context.setVariable("pnr", ticketHistory.getPnr());
                            context.setVariable("customerName", customerName);
                            context.setVariable("customerEmail", email);
                            context.setVariable("noOfPassengers", ticketHistory.getTotalNoOfPassengers());
                            context.setVariable("origin", ticketHistory.getOrigin());
                            context.setVariable("departureDateAndTime", formattedDepartureDate);
                            context.setVariable("destination", ticketHistory.getDestination());
                            context.setVariable("arrivalDateAndTime", formattedArrivalDate);
                            context.setVariable("flightNumber", ticketHistory.getFlightNumber());
                            context.setVariable("class", "ECONOMY");
                            context.setVariable("ticketAmount", ticketHistory.getTicketAmount());

                            // email
                            CompletableFuture<Void> completableFuture = notificationService.sendMail(email, "Flight less than 24 hours", "payment-remainder-2h", context);

                            completableFuture.thenRun(() -> {
                                payment.setReminderNotification(true);
                                paymentRepository.save(payment);
                                log.info("Notification sent and payment updated for PNR: {}", payment.getPnr());
                            }).exceptionally(ex -> {
                                log.error("Failed to send notification for PNR: {}", payment.getPnr(), ex);
                                return null;
                            });
                            // sms
                            String messageTemplate = "Reminder: Your flight with Hayaan Travel will depart in less 24 hours. Booking Ref: %s. Check your email for details.";

                            String formattedMessage = String.format(messageTemplate, ticketHistory.getPnr());

                            notificationService.sendSms(passenger.getPhoneNumber(), formattedMessage);

                        } else {
                            log.warn("No passengers found for ticket history ID: {}", ticketHistory.getId());
                        }
                    } else {
                        log.info("Departure date and time is not within the next 24 hours for PNR: {}", payment.getPnr());
                    }
                } else {
                    log.warn("No ticket history found for PNR: {}", payment.getPnr());
                }
            } else {
                log.info("PAYMENT IS PENDING BUT 2 HOURS NOT REACHED OR NOT TODAY");
            }
        }
    }

    // 2 hours after booking payment
    @Scheduled(fixedRate = 60000)
    public void paymentNotificationAfterTwoHours() {

        log.info("LOOKING UP PENDING PAYMENTS : {}");

        Optional<List<Payment>> byPaymentStatus = paymentRepository.findPaymentByPaymentStatusAndPendingNotification(0, false);

        if (!byPaymentStatus.isPresent() || byPaymentStatus.get().isEmpty()) {
            log.info("No pending payments");
            return;
        }

        List<Payment> pendingPayments = byPaymentStatus.get();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursAgo = now.minusHours(2);

        for (Payment payment : pendingPayments) {

            LocalDateTime paymentCreatedAt = payment.getCreatedAt();

            if (paymentCreatedAt.isBefore(twoHoursAgo) && paymentCreatedAt.toLocalDate().isEqual(now.toLocalDate())) {

                log.info("Yes triggering notification ======");


                Optional<TicketHistory> ticketByPnr = ticketHistoryRepo.findByPnr(payment.getPnr());

                if (ticketByPnr.isPresent()) {
                    TicketHistory ticketHistory = ticketByPnr.get();

                    String formattedArrivalDate = ticketHistory.getArrivalDateTime().format(DATE_TIME_FORMATTER);
                    ;
                    String formattedDepartureDate = ticketHistory.getDepartureDateTime().format(DATE_TIME_FORMATTER);
                    // query passenger
                    Optional<List<Passenger>> passengerList = passengerRepo.findByTicketHistoryId(ticketHistory.getId());

                    if (passengerList.isPresent() && !passengerList.get().isEmpty()) {
                        List<Passenger> passengers = passengerList.get();

                        Passenger passenger = passengers.get(0);
                        String email = passenger.getEmail();
                        String customerName = passenger.getFirstName() + passenger.getLastName();

                        Context context = new Context();
                        context.setVariable("pnr", ticketHistory.getPnr());
                        context.setVariable("customerName", customerName);
                        context.setVariable("customerEmail", email);
                        context.setVariable("noOfPassengers", ticketHistory.getTotalNoOfPassengers());
                        context.setVariable("origin", ticketHistory.getOrigin());
                        context.setVariable("departureDateAndTime", formattedDepartureDate);
                        context.setVariable("destination", ticketHistory.getDestination());
                        context.setVariable("arrivalDateAndTime", formattedArrivalDate);
                        context.setVariable("flightNumber", ticketHistory.getFlightNumber());
                        context.setVariable("class", "ECONOMY");
                        context.setVariable("ticketAmount", ticketHistory.getTicketAmount());


                        // email
                        CompletableFuture<Void> completableFuture = notificationService.sendMail(email, "Pending payment reminder", "payment-remainder-2h", context);

                        completableFuture.thenRun(() -> {
                            payment.setPendingNotification(true);
                            paymentRepository.save(payment);
                            log.info("Notification sent and payment updated for PNR: {}", payment.getPnr());
                        }).exceptionally(ex -> {
                            log.error("Failed to send notification for PNR: {}", payment.getPnr(), ex);
                            return null;
                        });

                        // sms
                        String messageTemplate = "Reminder: Complete your Hayaan Travel booking by paying %s. Booking Ref: %s. Check your email for details.";

                        String formattedMessage = String.format(messageTemplate, ticketHistory.getTicketAmount(), ticketHistory.getPnr());

                        notificationService.sendSms(passenger.getPhoneNumber(),
                                formattedMessage);

                    } else {
                        log.warn("No passengers found for ticket history ID: {}", ticketHistory.getId());
                    }
                } else {
                    log.warn("No ticket history found for PNR: {}", payment.getPnr());
                }

            } else {
                log.info("PAYMENT IS PENDING BUT 2 HOURS NOT REACHED OR NOT TODAY");
            }
        }
    }


    // booking with out payment reminder
    @Scheduled(fixedRate = 60000)
    public void ticketWithOutPayment() {

        Optional<List<Payment>> byPaymentStatus = paymentRepository.findPaymentByPaymentStatusAndWithOutPaymentNotification(0, false);

        if (!byPaymentStatus.isPresent() || byPaymentStatus.get().isEmpty()) {
            log.info("No success payment");
            return;
        }

        List<Payment> pendingPayments = byPaymentStatus.get();

        for (Payment payment : pendingPayments) {

            Optional<TicketHistory> ticketByPnr = ticketHistoryRepo.findByPnr(payment.getPnr());

            if (ticketByPnr.isPresent()) {
                TicketHistory ticketHistory = ticketByPnr.get();

                String formattedArrivalDate = ticketHistory.getArrivalDateTime().format(DATE_TIME_FORMATTER);
                ;
                String formattedDepartureDate = ticketHistory.getDepartureDateTime().format(DATE_TIME_FORMATTER);
                // query passenger
                Optional<List<Passenger>> passengerList = passengerRepo.findByTicketHistoryId(ticketHistory.getId());

                if (passengerList.isPresent() && !passengerList.get().isEmpty()) {
                    List<Passenger> passengers = passengerList.get();

                    Passenger passenger = passengers.get(0);
                    String email = passenger.getEmail();
                    String customerName = passenger.getFirstName() + passenger.getLastName();

                    Context context = new Context();
                    context.setVariable("pnr", ticketHistory.getPnr());
                    context.setVariable("customerName", customerName);
                    context.setVariable("customerEmail", email);
                    context.setVariable("noOfPassengers", ticketHistory.getTotalNoOfPassengers());
                    context.setVariable("origin", ticketHistory.getOrigin());
                    context.setVariable("departureDateAndTime", formattedDepartureDate);
                    context.setVariable("destination", ticketHistory.getDestination());
                    context.setVariable("arrivalDateAndTime", formattedArrivalDate);
                    context.setVariable("flightNumber", ticketHistory.getFlightNumber());
                    context.setVariable("class", "ECONOMY");
                    context.setVariable("ticketAmount", ticketHistory.getTicketAmount());

                    // email
                    CompletableFuture<Void> completableFuture = notificationService.sendMail(email, "Ticket payment reminder", "confirm-your-booking", context);

                    completableFuture.thenRun(() -> {
                        payment.setWithOutPaymentNotification(true);
                        paymentRepository.save(payment);
                        log.info("Notification sent and payment updated for PNR: {}", payment.getPnr());
                    }).exceptionally(ex -> {
                        log.error("Failed to send notification for PNR: {}", payment.getPnr(), ex);
                        return null;
                    });
                    // sms
                    String messageTemplate = "Dear %s. To confirm your Hayaan travel booking Booking Ref: %s. Please pay the amount : %s.";

                    String formattedMessage = String.format(messageTemplate, passenger.getFirstName(), ticketHistory.getPnr(), ticketHistory.getTicketAmount());

                    notificationService.sendSms(passenger.getPhoneNumber(),
                            formattedMessage);

                } else {
                    log.warn("No passengers found for ticket history ID: {}", ticketHistory.getId());
                }
            } else {
                log.warn("No ticket history found for PNR: {}", payment.getPnr());
            }


        }
    }

}
