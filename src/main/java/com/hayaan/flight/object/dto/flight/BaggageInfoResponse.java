package com.hayaan.flight.object.dto.flight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaggageInfoResponse {

    private int value;
    private String unit;
    private String allowedBaggage;

}
