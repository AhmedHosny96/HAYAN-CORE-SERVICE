package com.hayaan.flight.object.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStageDto {

    private String payerAccount;
    private String pnr;
    private Double amount;

}
