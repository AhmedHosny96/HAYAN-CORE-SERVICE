package com.hayaan.flight.object.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)

public class AirportListResp {

    private int status;
    private String message;
    private List<Object> airPortList;
    private List<Object> airLineList;
}
