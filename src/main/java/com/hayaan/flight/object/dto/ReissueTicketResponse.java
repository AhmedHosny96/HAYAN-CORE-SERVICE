package com.hayaan.flight.object.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReissueTicketResponse {

    private int status;
    private String message;
    private String uniqueReference;
    private String ptrStatus;
    private Long ptrUniqueID;
    private String ptrType;
    private String resolution;
}
