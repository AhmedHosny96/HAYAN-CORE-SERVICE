package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.entity.Country;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AllCountryResp {

    private int status;
    private String message;
    private List<Country> countries;
}
