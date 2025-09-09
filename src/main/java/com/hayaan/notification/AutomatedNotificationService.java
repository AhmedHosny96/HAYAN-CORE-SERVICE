package com.hayaan.notification;


import com.hayaan.config.UtilService;
import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.dto.BoardingDto;
import com.hayaan.flight.object.entity.Airport;
import com.hayaan.flight.object.entity.Passenger;
import com.hayaan.flight.object.entity.Payment;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.PassengerRepo;
import com.hayaan.flight.repo.PassengerTicketRepo;
import com.hayaan.flight.repo.PaymentRepository;
import com.hayaan.flight.repo.TicketHistoryRepo;
import com.hayaan.flight.service.FlightLogicService;
import com.hayaan.flight.service.PassengerTicket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.RequestBuilder;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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

    private final FlightLogicService flightLogicService;

    private final UtilService utilService;
    private final PassengerTicketRepo passengerTicketRepo;

    // a runner that checks if ticket is confirmed
//    public void checkConfirmedTicketsAndPopulate(){
//
//
//        List<Optional<Payment>> byPaymentStatus = paymentRepository.findByPaymentStatus(2);
//
//        // introduce new ticket status INPROCESS
//        // fetch all pending tickets (in process tickets) in a list
//        // call trip details api , if e-ticket is not empty update the e-ticket number and set the status to completed , send email and sms
//        // if not ignore
//        // run in 5 minutes
//    }

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void checkConfirmedTicketsAndPopulate() {
        List<Optional<Payment>> payments = paymentRepository.findByPaymentStatus(2);

        for (Optional<Payment> optionalPayment : payments) {
            if (!optionalPayment.isPresent()) continue;

            Payment payment = optionalPayment.get();
            Optional<TicketHistory> ticketOpt = ticketHistoryRepo.findByPnr(payment.getPnr());

            if (!ticketOpt.isPresent()) {
                log.warn("Ticket not found for PNR: {}", payment.getPnr());
                continue;
            }

            TicketHistory ticket = ticketOpt.get();
            JSONObject tripDetails = flightLogicService.fetchTripDetails(ticket.getPnr());

            JSONObject itineraryInfo = tripDetails.optJSONObject("ItineraryInfo");
            JSONObject customerInfo = itineraryInfo.optJSONArray("CustomerInfos").optJSONObject(0).optJSONObject("CustomerInfo");

            String eTicketNumber = customerInfo.optString("eTicketNumber");

            if (eTicketNumber == null || eTicketNumber.isBlank()) {
                log.info("E-ticket not issued yet for PNR: {}", ticket.getPnr());
                continue;
            }

            // Update ticket with e-ticket number
            ticket.setEticketNumber(eTicketNumber);
            ticket.setStatus(1); // Completed
            ticketHistoryRepo.save(ticket);

            List<PassengerTicket> passengersOpt = passengerTicketRepo.findByTicketHistory(ticket);

            if (!passengersOpt.isEmpty()) {

                Passenger passenger = passengersOpt.get(0).getPassenger();

                String email = passenger.getEmail();
                String customerName = passenger.getFirstName() + " " + passenger.getMiddleName() + " " + passenger.getLastName();

                String formattedDeparture = ticket.getDepartureDateTime().format(DATE_TIME_FORMATTER);
                String formattedArrival = ticket.getArrivalDateTime().format(DATE_TIME_FORMATTER);

                Airport originByCode = flightLogicService.getAirportByCode(ticket.getOrigin());
                Airport destinationByCode = flightLogicService.getAirportByCode(ticket.getDestination());

                BoardingDto boardingDto = new BoardingDto();
                boardingDto.setName(customerName.toUpperCase());
                boardingDto.setFlight(ticket.getFlightNumber());
                boardingDto.setSeat("-");
                boardingDto.setFrom(ticket.getOrigin() + " - " + originByCode.getCity() + ", " + originByCode.getCountry());
                boardingDto.setTo(ticket.getDestination() + " - " + destinationByCode.getCity() + ", " + destinationByCode.getCountry());
                boardingDto.setDepartureTime(formattedDeparture);
                boardingDto.setArrivalTime(formattedArrival);
                boardingDto.setPnr(ticket.getPnr());
                boardingDto.setETicketNumber(eTicketNumber);
                boardingDto.setTicketStatus("ISSUED");

                ByteArrayOutputStream pdfStream;
                try {
                    pdfStream = utilService.generatePdfWithCustomBarcode(boardingDto);
                } catch (Exception e) {
                    log.error("Failed to generate boarding pass for PNR: {}", ticket.getPnr(), e);
                    continue;
                }

                ByteArrayResource attachment = new ByteArrayResource(pdfStream.toByteArray()) {
                    @Override
                    public String getFilename() {
                        return "boarding-pass-" + ticket.getPnr() + ".pdf";
                    }
                };

                // Prepare and send email
                Context context = new Context();
                context.setVariable("pnr", ticket.getPnr());
                context.setVariable("customerName", customerName);
                context.setVariable("customerEmail", email);
                context.setVariable("noOfPassengers", ticket.getTotalNoOfPassengers());
                context.setVariable("origin", ticket.getOrigin());
                context.setVariable("departureDateAndTime", formattedDeparture);
                context.setVariable("destination", ticket.getDestination());
                context.setVariable("arrivalDateAndTime", formattedArrival);
                context.setVariable("flightNumber", ticket.getFlightNumber());
                context.setVariable("class", "ECONOMY");
                context.setVariable("ticketAmount", ticket.getTotalAmount());

                notificationService.sendMail(email, "Boarding Pass Ready", "boarding-pass", context, Optional.of(attachment))
                        .thenRun(() -> log.info("Boarding pass sent for PNR: {}", ticket.getPnr()))
                        .exceptionally(ex -> {
                            log.error("Failed to send boarding pass email for PNR: {}", ticket.getPnr(), ex);
                            return null;
                        });

                // Send SMS
                String smsMessage = String.format(
                        "Dear %s. Your booking is confirmed for Hayaan Travel. Ref: %s. Check your email for details.",
                        passenger.getFirstName(), ticket.getPnr()
                );
                notificationService.sendSms(passenger.getPhoneNumber(), smsMessage);

            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void successPaymentNotification() {
        Optional<List<Payment>> byPaymentStatus = paymentRepository.findByPaymentStatusAndSuccessPaymentNotification(2, false);

        if (!byPaymentStatus.isPresent() || byPaymentStatus.get().isEmpty()) {
            log.info("No successful payments found");
            return;
        }

        List<Payment> pendingPayments = byPaymentStatus.get();

        for (Payment payment : pendingPayments) {
            Optional<TicketHistory> ticketByPnr = ticketHistoryRepo.findByPnr(payment.getPnr());

            if (!ticketByPnr.isPresent()) {
                log.warn("No ticket found for PNR: {}", payment.getPnr());
                continue;
            }

            TicketHistory ticketHistory = ticketByPnr.get();

            List<PassengerTicket> passengerTickets = passengerTicketRepo.findByTicketHistory(ticketHistory);
            if (passengerTickets.isEmpty()) {
                log.warn("No passengers mapped to ticket history ID: {}", ticketHistory.getId());
                continue;
            }
            Passenger passenger = passengerTickets.get(0).getPassenger();
            String email = passenger.getEmail();
            String customerName = passenger.getFirstName() + " " + passenger.getMiddleName() + " " + passenger.getLastName();
            String formattedDepartureDate = ticketHistory.getDepartureDateTime().format(DATE_TIME_FORMATTER);
            String formattedArrivalDate = ticketHistory.getArrivalDateTime().format(DATE_TIME_FORMATTER);

            // Email context
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
            context.setVariable("ticketAmount", ticketHistory.getTotalAmount());

            notificationService.sendMail(email, "Payment received!", "success-payment", context, Optional.empty())
                    .thenRun(() -> {
                        payment.setSuccessPaymentNotification(true);
                        payment.setPaymentStatus(3);
                        payment.setPaymentStatusDesc("INPROCESS");
                        paymentRepository.save(payment);
                        log.info("Email sent and payment marked in-process for PNR: {}", payment.getPnr());
                    })
                    .exceptionally(ex -> {
                        log.error("Failed to send email for PNR: {}", payment.getPnr(), ex);
                        return null;
                    });

            // Send SMS
            String smsMessage = String.format(
                    "Dear %s. Payment received! You will receive details via email once your ticket is confirmed!",
                    passenger.getFirstName(), ticketHistory.getPnr()
            );
            notificationService.sendSms(passenger.getPhoneNumber(), smsMessage);
        }
    }


    // success payment notification
//    @Scheduled(fixedRate = 60000)
//    public void successPaymentNotification() {
//        // Fetch payments where the status is 2 and notification has not yet been sent
//        Optional<List<Payment>> byPaymentStatus = paymentRepository.findByPaymentStatusAndSuccessPaymentNotification(2, false);
//
//        if (!byPaymentStatus.isPresent() || byPaymentStatus.get().isEmpty()) {
//            log.info("No successful payments found");
//            return;
//        }
//
//        List<Payment> pendingPayments = byPaymentStatus.get();
//
//        for (Payment payment : pendingPayments) {
//            // Confirm the ticket
//            CustomResponse confirmationResponse = flightLogicService.confirmTicket(payment.getPnr());
//
//            if (confirmationResponse.status() != 200) {
//                log.warn("Ticket confirmation failed for PNR: {}. Reason: {}", payment.getPnr(), confirmationResponse.message());
//                continue; // Skip to the next payment if confirmation fails
//            }
//
//            log.info("Ticket confirmed successfully for PNR: {}", payment.getPnr());
//
//            // Check if the ticket associated with the payment has a status of 1
//            Optional<TicketHistory> ticketByPnr = ticketHistoryRepo.findByPnrAndStatus(payment.getPnr(), 1);
//
//            if (ticketByPnr.isPresent()) {
//                TicketHistory ticketHistory = ticketByPnr.get();
//
//                // Fetch additional trip details
//                JSONObject fetchTripDetailsResponse = flightLogicService.fetchTripDetails(ticketHistory.getPnr());
//
//                JSONObject itineraryInfo = fetchTripDetailsResponse.optJSONObject("ItineraryInfo");
//                JSONObject customerInfo = itineraryInfo.optJSONArray("CustomerInfos").optJSONObject(0).optJSONObject("CustomerInfo");
//                JSONObject reservationItem = itineraryInfo.optJSONArray("ReservationItems").optJSONObject(0).optJSONObject("ReservationItem");
//
//                String eTicketNumber = customerInfo.optString("eTicketNumber");
//                String airlinePNR = reservationItem.optString("AirlinePNR");
//
//                String formattedArrivalDate = ticketHistory.getArrivalDateTime().format(DATE_TIME_FORMATTER);
//                String formattedDepartureDate = ticketHistory.getDepartureDateTime().format(DATE_TIME_FORMATTER);
//
//                Optional<List<Passenger>> passengerList = passengerRepo.findByTicketHistoryId(ticketHistory.getId());
//
//                if (passengerList.isPresent() && !passengerList.get().isEmpty()) {
//                    List<Passenger> passengers = passengerList.get();
//                    Passenger passenger = passengers.get(0);
//
//                    String origin = ticketHistory.getOrigin();
//                    String destination = ticketHistory.getDestination();
//
//                    Airport originByCode = flightLogicService.getAirportByCode(origin);
//                    Airport destinationByCode = flightLogicService.getAirportByCode(destination);
//
//                    String email = passenger.getEmail();
//                    String customerName = passenger.getFirstName() + " " + passenger.getMiddleName() + " " + passenger.getLastName();
//
//                    // Generate the PDF boarding pass in memory
//                    BoardingDto boardingDto = new BoardingDto();
//                    boardingDto.setName(customerName.toUpperCase());
//                    boardingDto.setFlight(ticketHistory.getFlightNumber());
//                    boardingDto.setSeat("-"); // Example seat number
//                    boardingDto.setFrom(origin + " - " + originByCode.getCity() + " , " + originByCode.getCountry());
//                    boardingDto.setTo(destination + " - " + destinationByCode.getCity() + " , " + destinationByCode.getCountry());
//                    boardingDto.setDepartureTime(formattedDepartureDate);
//                    boardingDto.setArrivalTime(formattedArrivalDate);
//                    boardingDto.setPnr(airlinePNR);
//                    boardingDto.setETicketNumber(eTicketNumber);
//                    boardingDto.setTicketStatus("ISSUED");
//
//                    // Generate the PDF as ByteArrayOutputStream
//                    ByteArrayOutputStream pdfStream;
//                    try {
//                        pdfStream = utilService.generatePdfWithCustomBarcode(boardingDto);
//                    } catch (Exception e) {
//                        log.error("Error generating PDF for PNR: {}", ticketHistory.getPnr(), e);
//                        continue; // Skip to the next payment if PDF generation fails
//                    }
//
//                    // Convert ByteArrayOutputStream to ByteArrayResource to send it via email
//                    ByteArrayResource byteArrayResource = new ByteArrayResource(pdfStream.toByteArray()) {
//                        @Override
//                        public String getFilename() {
//                            return "boarding-pass-" + ticketHistory.getPnr() + ".pdf";
//                        }
//                    };
//
//                    // Prepare email content
//                    Context context = new Context();
//                    context.setVariable("pnr", ticketHistory.getPnr());
//                    context.setVariable("customerName", customerName);
//                    context.setVariable("customerEmail", email);
//                    context.setVariable("noOfPassengers", ticketHistory.getTotalNoOfPassengers());
//                    context.setVariable("origin", ticketHistory.getOrigin());
//                    context.setVariable("departureDateAndTime", formattedDepartureDate);
//                    context.setVariable("destination", ticketHistory.getDestination());
//                    context.setVariable("arrivalDateAndTime", formattedArrivalDate);
//                    context.setVariable("flightNumber", ticketHistory.getFlightNumber());
//                    context.setVariable("class", "ECONOMY");
//                    context.setVariable("ticketAmount", ticketHistory.getTotalAmount());
//
//                    // Send email with PDF attachment
//                    CompletableFuture<Void> completableFuture = notificationService.sendMail(email, "Success payment notification", "success-payment", context, Optional.of(byteArrayResource) // Attach the PDF as ByteArrayResource
//                    );
//
//                    completableFuture.thenRun(() -> {
//                        // Update payment to indicate notification was sent
    ////                        payment.setSuccessPaymentNotification(true);
    ////                        payment.setPendingNotification(true);
//                        payment.setPaymentStatus(2);
//                        payment.setPaymentStatusDesc("INPROCESS");
//                        paymentRepository.save(payment);
//                        log.info("Notification sent and payment updated for PNR: {}", payment.getPnr());
//                    }).exceptionally(ex -> {
//                        log.error("Failed to send email notification for PNR: {}", payment.getPnr(), ex);
//                        return null;
//                    });
//
//                    // Send SMS
//                    String messageTemplate = "Dear %s. Payment received! Your Hayaan Travel booking is confirmed. Booking Ref: %s. Check your email for details.";
//                    String formattedMessage = String.format(messageTemplate, passenger.getFirstName(), ticketHistory.getPnr());
//                    notificationService.sendSms(passenger.getPhoneNumber(), formattedMessage);
//
//                } else {
//                    log.warn("No passengers found for ticket history ID: {}", ticketHistory.getId());
//                }
//            } else {
//                log.warn("Success payment Notification: No ticket history with status 1 found for PNR: {}", payment.getPnr());
//            }
//        }
//    }

    // 24 hours before flight
    @Scheduled(fixedRate = 60000)
    public void twentyFourHourPriorReminder() {

        log.info("LOOKING UP PENDING PAYMENTS : {}");

        Optional<List<Payment>> byPaymentStatus = paymentRepository.findPaymentByPaymentStatusAndReminderNotification(0, false);

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
                        List<PassengerTicket> passengerTickets = passengerTicketRepo.findByTicketHistory(ticketHistory);


                        if (!passengerTickets.isEmpty()) {

                            Passenger passenger = passengerTickets.get(0).getPassenger();
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
                            context.setVariable("ticketAmount", ticketHistory.getTotalAmount());

                            // email
                            CompletableFuture<Void> completableFuture = notificationService.sendMail(email, "Flight less than 24 hours", "payment-remainder-2h", context, Optional.empty());

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
        log.info("LOOKING UP PENDING PAYMENTS");

        Optional<List<Payment>> optionalPayments = paymentRepository.findPaymentByPaymentStatusAndPendingNotification(0, false);

        if (!optionalPayments.isPresent() || optionalPayments.get().isEmpty()) {
            log.info("No pending payments found.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursAgo = now.minusHours(2);

        for (Payment payment : optionalPayments.get()) {
            LocalDateTime createdAt = payment.getCreatedAt();

            if (!createdAt.toLocalDate().isEqual(now.toLocalDate()) || createdAt.isAfter(twoHoursAgo)) {
                log.info("Payment created less than 2 hours ago or not today. Skipping PNR: {}", payment.getPnr());
                continue;
            }

            Optional<TicketHistory> ticketOpt = ticketHistoryRepo.findByPnr(payment.getPnr());
            if (!ticketOpt.isPresent()) {
                log.warn("No ticket history found for PNR: {}", payment.getPnr());
                continue;
            }

            TicketHistory ticket = ticketOpt.get();
            List<PassengerTicket> passengerTickets = passengerTicketRepo.findByTicketHistory(ticket);

            if (!passengerTickets.isEmpty()) {
                log.warn("No passengers found for ticket history ID: {}", ticket.getId());
                continue;
            }

            Passenger passenger = passengerTickets.get(0).getPassenger();
            String email = passenger.getEmail();
            String phone = passenger.getPhoneNumber();
            String customerName = passenger.getFirstName() + " " + passenger.getLastName();

            String formattedDeparture = ticket.getDepartureDateTime().format(DATE_TIME_FORMATTER);
            String formattedArrival = ticket.getArrivalDateTime().format(DATE_TIME_FORMATTER);

            Context context = new Context();
            context.setVariable("pnr", ticket.getPnr());
            context.setVariable("customerName", customerName);
            context.setVariable("customerEmail", email);
            context.setVariable("noOfPassengers", ticket.getTotalNoOfPassengers());
            context.setVariable("origin", ticket.getOrigin());
            context.setVariable("departureDateAndTime", formattedDeparture);
            context.setVariable("destination", ticket.getDestination());
            context.setVariable("arrivalDateAndTime", formattedArrival);
            context.setVariable("flightNumber", ticket.getFlightNumber());
            context.setVariable("class", "ECONOMY");
            context.setVariable("ticketAmount", ticket.getTotalAmount());

            // Send email
            notificationService.sendMail(email, "Pending payment reminder", "payment-remainder-2h", context, Optional.empty())
                    .thenAcceptAsync(v -> {
                        // Send SMS only after email success
                        try {
                            String smsTemplate = "Reminder: Complete your Hayaan Travel booking by paying %s. Booking Ref: %s. Check your email for details.";
                            String smsMessage = String.format(smsTemplate, ticket.getTotalAmount(), ticket.getPnr());
                            notificationService.sendSms(phone, smsMessage);

                            // Mark as notified after both email and SMS
                            payment.setPendingNotification(true);
                            paymentRepository.save(payment);
                            log.info("Email & SMS sent. Notification flag updated for PNR: {}", ticket.getPnr());

                        } catch (Exception smsEx) {
                            log.error("Failed to send SMS for PNR: {}", ticket.getPnr(), smsEx);
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Failed to send email for PNR: {}", ticket.getPnr(), ex);
                        return null;
                    });
        }
    }


    // booking with out payment reminder
    @Scheduled(fixedRate = 60000)
    public void ticketWithOutPayment() {
        Optional<List<Payment>> optionalPayments = paymentRepository.findPaymentByPaymentStatusAndWithOutPaymentNotification(0, false);

        if (!optionalPayments.isPresent() || optionalPayments.get().isEmpty()) {
            log.info("No unpaid bookings found for notification.");
            return;
        }

        List<Payment> pendingPayments = optionalPayments.get();

        for (Payment payment : pendingPayments) {
            // Skip if already notified
            if (Boolean.TRUE.equals(payment.getWithOutPaymentNotification())) {
                log.info("Notification already sent for PNR: {}", payment.getPnr());
                continue;
            }

            Optional<TicketHistory> optionalTicket = ticketHistoryRepo.findByPnrAndStatus(payment.getPnr(), 0);
            if (!optionalTicket.isPresent()) {
                log.warn("No booked ticket history found for PNR: {}", payment.getPnr());
                continue;
            }

            TicketHistory ticketHistory = optionalTicket.get();
            List<PassengerTicket> passengerTickets = passengerTicketRepo.findByTicketHistory(ticketHistory);

            if (!passengerTickets.isEmpty()) {
                log.warn("No passengers found for ticket history ID: {}", ticketHistory.getId());
                continue;
            }

            Passenger passenger = passengerTickets.get(0).getPassenger();
            String customerName = passenger.getFirstName() + " " + passenger.getLastName();
            String email = passenger.getEmail();
            String phone = passenger.getPhoneNumber();

            // Format flight info
            String formattedDeparture = ticketHistory.getDepartureDateTime().format(DATE_TIME_FORMATTER);
            String formattedArrival = ticketHistory.getArrivalDateTime().format(DATE_TIME_FORMATTER);

            // Email context setup
            Context context = new Context();
            context.setVariable("pnr", ticketHistory.getPnr());
            context.setVariable("customerName", customerName);
            context.setVariable("customerEmail", email);
            context.setVariable("noOfPassengers", ticketHistory.getTotalNoOfPassengers());
            context.setVariable("origin", ticketHistory.getOrigin());
            context.setVariable("departureDateAndTime", formattedDeparture);
            context.setVariable("destination", ticketHistory.getDestination());
            context.setVariable("arrivalDateAndTime", formattedArrival);
            context.setVariable("flightNumber", ticketHistory.getFlightNumber());
            context.setVariable("class", "ECONOMY");
            context.setVariable("ticketAmount", ticketHistory.getTotalAmount());

            // Generate PDF
            ByteArrayOutputStream pdfStream;
            try {
                BoardingDto boardingDto = new BoardingDto();
                boardingDto.setName(customerName.toUpperCase());
                boardingDto.setFlight(ticketHistory.getFlightNumber());
                boardingDto.setSeat("-");
                boardingDto.setFrom(ticketHistory.getOrigin());
                boardingDto.setTo(ticketHistory.getDestination());
                boardingDto.setDepartureTime(formattedDeparture);
                boardingDto.setArrivalTime(formattedArrival);
                boardingDto.setPnr(ticketHistory.getPnr());
                boardingDto.setETicketNumber("N/A");
                boardingDto.setTicketStatus("BOOKED");

                pdfStream = utilService.generatePdfWithCustomBarcode(boardingDto);
            } catch (Exception e) {
                log.error("Failed to generate PDF for PNR: {}", ticketHistory.getPnr(), e);
                continue;
            }

            ByteArrayResource attachment = new ByteArrayResource(pdfStream.toByteArray()) {
                @Override
                public String getFilename() {
                    return "boarding-pass-" + ticketHistory.getPnr() + ".pdf";
                }
            };

            // Send Email
            notificationService.sendMail(email, "Ticket payment reminder", "confirm-your-booking", context, Optional.of(attachment))
                    .thenRun(() -> {
                        payment.setWithOutPaymentNotification(true);
                        paymentRepository.save(payment);
                        log.info("Email sent and notification flag updated for PNR: {}", payment.getPnr());
                    }).exceptionally(ex -> {
                        log.error("Failed to send email for PNR: {}", payment.getPnr(), ex);
                        return null;
                    });

            // Send SMS
            String smsTemplate = "Dear %s. To confirm your Hayaan travel booking Booking Ref: %s. Please pay the amount : %s.";
            String smsContent = String.format(smsTemplate, passenger.getFirstName(), ticketHistory.getPnr(), ticketHistory.getTotalAmount());
            notificationService.sendSms(phone, smsContent);
        }
    }


//    @Scheduled(fixedRate = 60000)
//    public void updatePaymentNotifications() {
//
//        List<Optional<Payment>> byPaymentStatus = paymentRepository.findByPaymentStatus(2);
//
//        if (!byPaymentStatus.isEmpty()) {
//            byPaymentStatus.stream()
//                    .filter(Optional::isPresent)  // Ensure we only process non-empty Optionals
//                    .map(Optional::get)            // Unwrap the Optional to get the Payment object
//                    .forEach(payment -> {
//                        try {
//                            // Update notification flags
//                            payment.setSuccessPaymentNotification(true);
//                            payment.setWithOutPaymentNotification(true);
//                            payment.setPendingNotification(true);
//                            payment.setReminderNotification(true);
//
//                            // Save the updated payment entity
//                            paymentRepository.save(payment);
//
//                            log.info("Payment notifications updated for payment ID: {}", payment.getId());
//                        } catch (Exception e) {
//                            log.error("Error updating payment notifications for payment ID: {}", payment.getId(), e);
//                        }
//                    });
//        } else {
//            log.info("No payments found with status 2 that require notification updates.");
//        }
//    }

}