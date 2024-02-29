package com.hayaan.flight.object.dto.flight;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlightSearchResponse {

    private int status;
    private String message;
    private String key;
    private PriceInfoResponse priceInfo;
    private List<AirInfoResponse> airInfo;
    private List<PassengerCriteria> passengerInfo;
    private BaggageInfoResponse fairInfo;

}
