package com.hayaan.flight.object.dto.booking;

import com.hayaan.flight.object.dto.AirPriceSolution;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookingRequestDto {

    private List<TravelersDto> travelers;
    private List<AirPriceSolution> airPriceSolution;
    private List<FormOfPaymentDto> formOfPayment;
}
