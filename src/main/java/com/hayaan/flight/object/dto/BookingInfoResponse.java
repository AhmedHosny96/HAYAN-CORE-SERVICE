package com.hayaan.flight.object.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingInfoResponse {

    private String bookingCode;
    private String cabinClass;
    private String segmentRef;
    private String fareInfoRef;
    private String hostTokenRef;

}