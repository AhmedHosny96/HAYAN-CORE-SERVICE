package com.hayaan.flight.service;

import com.hayaan.flight.object.entity.Airline;
import com.hayaan.flight.object.entity.Airport;
import com.hayaan.flight.repo.AirlineRepository;
import com.hayaan.flight.repo.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AirDetailsService {


    private final AirportRepository airportRepository;

    private final AirlineRepository airlineRepository;


    public Airport getAirportNameByCode(String code) {
        Optional<Airport> airportByAirportCode = airportRepository.findAirportByAirportCode(code);

//        if (!airportByAirportCode.isPresent()) {
//            return "UNKNOWN";
//        }

        return airportByAirportCode.get();
    }

    public String getAirlineNameByCode(String code) {
        Optional<Airline> airportByAirportCode = airlineRepository.findByAirLineCode(code);

        if (!airportByAirportCode.isPresent()) {
            return "UNKNOWN";
        }
        return airportByAirportCode.get().getAirLineName();
    }
}
