package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPnrAndPaymentStatus(String pnr, Integer status);

    Optional<Payment> findByPnr(String pnr);

}
