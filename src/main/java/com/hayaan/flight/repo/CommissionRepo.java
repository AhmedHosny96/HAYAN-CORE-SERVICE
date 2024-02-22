package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommissionRepo extends JpaRepository<Commission , Integer> {
}
