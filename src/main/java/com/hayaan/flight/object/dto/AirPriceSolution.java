package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.dto.flight.AirInfoResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AirPriceSolution {

    private int status;
    private String message;
    private List<AirPriceInfoResponse> airPriceInfo;
    private List<PassengerType> passengerInfo;


}
