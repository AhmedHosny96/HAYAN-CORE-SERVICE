package com.hayaan.flight.object.dto;


import com.hayaan.flight.object.entity.Commission;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserCommissionResponse {

    private int status;
    private String message;

    private List<Commission> commissions;
}
