package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Integer> {

    Optional<Airport> findAirportByAirportCode(String code);
}
