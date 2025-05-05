package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPnrAndPaymentStatus(String pnr, Integer status);

    Optional<Payment> findByPnr(String pnr);

    // 2 hours before ticket expires

    Optional<List<Payment>> findPaymentByPaymentStatusAndPendingNotification(int status, boolean isPendingNotified);

    // success payment notification
    Optional<List<Payment>> findByPaymentStatusAndSuccessPaymentNotification(int status, boolean isSuccessPaymentNotified);

    // 24 notification

    Optional<List<Payment>> findPaymentByPaymentStatusAndReminderNotification(int status, boolean isSuccessPaymentNotified);

    // ticket with out payment
    Optional<List<Payment>> findPaymentByPaymentStatusAndWithOutPaymentNotification(int status, boolean isWithOutPayment);


   List<Optional<Payment>> findByPaymentStatus(Integer status);

    boolean existsByPnr(String pnr);
}
