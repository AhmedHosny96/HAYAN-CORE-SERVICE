package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.entity.Commission;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommissionResponse {

    private int status;
    private String message;
    private Commission commission;
}
