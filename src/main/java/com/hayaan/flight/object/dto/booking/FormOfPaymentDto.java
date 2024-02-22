package com.hayaan.flight.object.dto.booking;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FormOfPaymentDto {

    private String key;
    private Boolean isCash;
}
