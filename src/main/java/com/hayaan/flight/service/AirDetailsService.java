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

    // is flight international

    public boolean isInternationalFlight(String from, String to) {
        Optional<Airport> fromAirportOpt = airportRepository.findAirportByAirportCode(from);
        Optional<Airport> toAirportOpt = airportRepository.findAirportByAirportCode(to);

        if (fromAirportOpt.isPresent() && toAirportOpt.isPresent()) {
            Airport fromAirport = fromAirportOpt.get();
            Airport toAirport = toAirportOpt.get();
            return !fromAirport.getCountry().equalsIgnoreCase(toAirport.getCountry());
        } else {
            // Handle the case where one or both airports are not found
            throw new IllegalArgumentException("One or both airports not found.");
        }
    }

    // round values

    public double roundNumber(double value) {

        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
