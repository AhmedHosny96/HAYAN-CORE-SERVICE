package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.entity.Currency;
import com.hayaan.flight.object.entity.CurrencyConversion;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class CurrencyConversionListResp {

        private int status;
        private String message;
        private List<CurrencyConversion> currencyConversions;


}
