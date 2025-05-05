package com.hayaan.flight.object.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsertConversionHistoryDb {

    private String bookingReference;

    private String baseCurrency;

    private String targetCurrency;

    private Double rate;

    private Double originalAmount;

    private Double amountAfter;

}
