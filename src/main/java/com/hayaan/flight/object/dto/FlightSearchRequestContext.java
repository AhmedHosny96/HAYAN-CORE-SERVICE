package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.dto.flight.FlightSearchDto;
import org.springframework.stereotype.Service;

@Service
public class FlightSearchRequestContext {


    private static final ThreadLocal<FlightSearchDto> currentRequest = new ThreadLocal<>();

    public void setCurrentRequest(FlightSearchDto request) {
        currentRequest.set(request);
    }

    public FlightSearchDto getCurrentRequest() {
        return currentRequest.get();
    }

    public void clear() {
        currentRequest.remove();
    }


}
