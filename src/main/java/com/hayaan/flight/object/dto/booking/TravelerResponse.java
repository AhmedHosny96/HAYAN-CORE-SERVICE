package com.hayaan.flight.object.dto.booking;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TravelerResponse {

    private String prefix;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String location;
    private String phoneNumber;
}
