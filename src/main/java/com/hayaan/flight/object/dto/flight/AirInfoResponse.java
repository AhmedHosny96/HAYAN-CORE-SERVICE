package com.hayaan.flight.object.dto.flight;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)

public class AirInfoResponse {

    private String sessionId;
    private String fareSourceCode;
    private String key;
    private String carrier;
    private String flightNumber;
    private String equipment;
    private int FlightTime;
    private LocalDate departureDate;
    private String departureTime;
    private LocalDate arrivalDate;
    private String arrivalTime;
    private String origin;
    private String destination;
    private String classOfService;
    private int remainingSeats;

    // for flight logic
    private Boolean isRefundable;
    private int totalStops;

    private Double totalAmount;
    private String currency;

}
