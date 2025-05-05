package com.hayaan.flight.object.dto.booking;

import com.hayaan.dto.PassengerType;
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
    private List<PhoneNumber> phoneNumbers; // Changed to List<PhoneNumber>
    private String email;
    private Address address; // Changed to Address type
    private PassengerType travelerType;
    private String idNo;
    private String nationality;
    private LocalDate passportExpiryDate;
    private String frequentFlyerNumber;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneNumber {
        private String phoneNumber;
        private String areaCode;
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

