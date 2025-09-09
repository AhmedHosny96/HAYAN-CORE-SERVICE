package com.hayaan.flight.object.dto.booking;

import com.hayaan.dto.PassengerType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TravelerResponse {
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String passengerType;
    private String gender;
    private String address;
    private String email;
    private String phoneNumber;
}
