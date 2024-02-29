package com.hayaan.flight.object.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FareInfoResponse {

    private String key;
    private String fareBasis;
    private String passengerTypeCode;
    private String origin;
    private String destination;

}