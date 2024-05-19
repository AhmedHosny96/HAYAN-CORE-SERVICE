package com.hayaan.flight.object.dto.flight;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class PriceInfoResponse {

    private String currency;
    private double originalPrice;
    //    private double taxes;
    private double commission;
    private double priceAfterTaxAndCommission;

    private double taxAmount;
    private double baseFareAmount;
    private double totalAmount;
    private double changePenaltyAmount;
    private double refundPenaltyAmount;
    private boolean changeAllowed;
    private boolean refundAllowed;


}
