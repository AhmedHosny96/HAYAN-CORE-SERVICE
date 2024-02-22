package com.hayaan.flight.object.dto.flight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirInfoResponse {

    private String key;
    private String carrier;
    private String flightNumber;
    private String equipment;
    private LocalDate departureDate;
    private String departureTime;
    private LocalDate arrivalDate;
    private String arrivalTime;
    private String origin;
    private String destination;
    private String classOfService;
    private int FlightTime;

}
