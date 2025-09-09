package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.entity.Passenger;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PassengerResp {

    private int status;
    private String message;
    private Passenger passenger;
}
