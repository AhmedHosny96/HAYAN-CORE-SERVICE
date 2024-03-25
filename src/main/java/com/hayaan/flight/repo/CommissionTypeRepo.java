package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.Commission;
import com.hayaan.flight.object.entity.CommissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommissionTypeRepo extends JpaRepository<CommissionType, Integer> {

    Optional<CommissionType> findByType(String type);

    CommissionType findFirstByOrderById();
}
