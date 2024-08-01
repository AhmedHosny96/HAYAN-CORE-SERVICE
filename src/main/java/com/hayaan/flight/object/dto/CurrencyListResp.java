package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.entity.Currency;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CurrencyListResp {

    private int status;
    private String message;
    private List<Currency> currencies;
    
}
