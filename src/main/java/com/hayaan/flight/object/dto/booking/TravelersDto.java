package com.hayaan.flight.object.dto.booking;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TravelersDto {

    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private List<PhoneNumber> phoneNumber;
    private Address address;
    private String travelerType;


    public class PhoneNumber {
        private String phoneNumber;
        private String areCode;
        private String cityCode;
        private String countryArea;

        // Getters and setters

    }

    public class Address {
        private String city;
        private String country;
        private String street;
        private String postalCode;

        // Getters and setters
    }
}

