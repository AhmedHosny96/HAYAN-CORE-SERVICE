package com.hayaan.flight.object.dto.flight;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FlightPriceSearchDto {

    private List<PassengerCriteria> passengersInfo;
    private List<AirInfoResponse> airInfo;
}
