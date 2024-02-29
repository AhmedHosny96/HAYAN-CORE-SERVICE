package com.hayaan.flight.object.dto.flight;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hayaan.flight.object.dto.BookingInfoResponse;
import com.hayaan.flight.object.dto.FareInfoResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class AirObjectInfo {

    private String key;
    private List<FareInfoResponse> fareInfo;
    private List<BookingInfoResponse> bookingInfo;
}
