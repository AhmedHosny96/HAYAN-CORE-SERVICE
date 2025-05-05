package com.hayaan.flight.object.dto.booking;

import com.hayaan.dto.PassengerType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TravelerResponse {

    private String passengerType;
    private String prefix;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String location;
    private String phoneNumber;
}
