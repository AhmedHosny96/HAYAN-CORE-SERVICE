package com.hayaan.flight.repo;

import com.hayaan.auth.object.entity.User;
import com.hayaan.flight.object.FlightType;
import com.hayaan.flight.object.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRepo extends JpaRepository<Commission, Integer> {

    Optional<List<Commission>> findByUser(User user);

    Optional<Commission> findByUserAndFlightType(User user , FlightType flightType);
}
