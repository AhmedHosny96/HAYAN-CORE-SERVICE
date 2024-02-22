package com.hayaan.flight.object.dto.flight;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hayaan.flight.object.dto.BookingInfoResponse;
import com.hayaan.flight.object.dto.FareInfoResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class AirObjectInfo {

    private String key;
    private FareInfoResponse fareInfo;
    private BookingInfoResponse bookingInfo;
}
