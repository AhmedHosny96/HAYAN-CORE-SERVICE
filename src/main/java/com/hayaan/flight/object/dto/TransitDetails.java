package com.hayaan.flight.object.dto;

import lombok.Data;

import java.time.Duration;

@Data
public class TransitDetails {

    private String airportCode;
    private String country;
    private String airlineName;
    private String airlineLogo;
    private String city;
    private Duration layoverDuration;
    private String arrivalDateTime;
    private String departureDateTime;

}
