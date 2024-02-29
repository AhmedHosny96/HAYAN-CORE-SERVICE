package com.hayaan.flight.object.dto;


import com.hayaan.flight.object.dto.flight.AirInfoResponse;
import com.hayaan.flight.object.dto.flight.AirObjectInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AirPriceInfoResponse {

    private String key;
    private List<HostTokenResponse> hostToken;
    private List<AirInfoResponse> airSegment;
    private List<AirObjectInfo> airPricingInfo;

    // another object which will contain key and two below objects


}
