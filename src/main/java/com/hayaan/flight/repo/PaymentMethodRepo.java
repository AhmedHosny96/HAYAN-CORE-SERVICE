package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepo extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByStatus(int status);
}
