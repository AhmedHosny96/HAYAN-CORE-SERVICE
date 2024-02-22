package com.hayaan.flight.object.dto.flight;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceInfoResponse {

    private String currency;
    private double originalPrice;
    private double taxes;
    private double commission;
    private double priceAfterTaxAndCommission;

}
