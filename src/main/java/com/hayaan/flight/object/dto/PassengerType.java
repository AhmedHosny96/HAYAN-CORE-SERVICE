package com.hayaan.flight.object.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PassengerType {

    private String code;
    private int age;
}
