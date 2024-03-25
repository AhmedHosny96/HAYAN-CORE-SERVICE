package com.hayaan.flight.object.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TravelersDto {

    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private List<PhoneNumber> phoneNumber;
    private String email;
    private Address address;
    private String travelerType;
    private String idNo;
    private String nationality;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneNumber {
        private String phoneNumber;
        private String areCode;
        private String cityCode;
        private String countryArea;

        // Getters and setters

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String city;
        private String country;
        private String street;
        private String postalCode;

        // Getters and setters
    }
}

