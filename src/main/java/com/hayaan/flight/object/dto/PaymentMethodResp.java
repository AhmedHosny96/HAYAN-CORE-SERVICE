package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.entity.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaymentMethodResp {

    private int status;
    private String message;
    private List<PaymentMethod> paymentMethods;
}
