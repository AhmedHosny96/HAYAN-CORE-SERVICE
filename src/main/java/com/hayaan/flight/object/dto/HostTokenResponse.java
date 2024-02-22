package com.hayaan.flight.object.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostTokenResponse {

    private String key;
    private String value;

}
