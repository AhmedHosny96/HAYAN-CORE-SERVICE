package com.hayaan.flight.object.dto.booking;

import lombok.Data;

@Data
public class BookingPaymentDto {

    private String pnr;
    private String paymentMethod;
    private String payerAccount;
}
