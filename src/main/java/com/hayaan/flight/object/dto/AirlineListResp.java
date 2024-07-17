package com.hayaan.flight.object.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hayaan.flight.object.entity.Airline;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)

public class AirlineListResp {
    
    private int status;
    private String message;
    private List<Airline> airLineList;


}
