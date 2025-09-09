package com.hayaan.flight.object.dto.booking;

import com.hayaan.flight.object.dto.AirPriceInfoResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookingRequestDto {

    //@NotBlank(message = "Currency can't be empty")
    private String currency;
    private List<TravelersDto> travelers;
    private List<AirPriceInfoResponse> airPriceInfo;
    private Long userId;
    private Long agentId;


}
