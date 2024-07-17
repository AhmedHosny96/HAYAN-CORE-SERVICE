package com.hayaan.flight.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfoResponse {

    private String bookingCode;
    private String cabinClass;
    private String segmentRef;
    private String fareInfoRef;
    private String hostTokenRef;

}