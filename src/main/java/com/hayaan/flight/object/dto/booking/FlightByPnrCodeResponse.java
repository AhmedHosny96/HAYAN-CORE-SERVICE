package com.hayaan.flight.object.dto.booking;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hayaan.flight.object.dto.BookingInfoResponse;
import com.hayaan.flight.object.dto.PassengerType;
import com.hayaan.flight.object.dto.flight.AirInfoResponse;
import com.hayaan.flight.object.dto.flight.DepartFlightResponse;
import com.hayaan.flight.object.dto.flight.PriceInfoResponse;
import com.hayaan.flight.object.dto.flight.ReturnFlightResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlightByPnrCodeResponse {

    private int status;
    private String message;
    private String pnrCode;
    private int version;
    private String bookingStatus;

    private List<TravelerResponse> travelers;

    private PriceInfoResponse priceInfo;

    private List<DepartFlightResponse> onwardFlight;
    private List<ReturnFlightResponse> returnFlight;

    private List<BookingInfoResponse> bookingInfo;

    private List<PassengerType> passengerInfo;

}