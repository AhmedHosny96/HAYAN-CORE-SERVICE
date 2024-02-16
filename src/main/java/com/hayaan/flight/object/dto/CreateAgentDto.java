package com.hayaan.flight.object.dto;

import com.hayaan.flight.object.entity.AgentType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateAgentDto {
    private String name;
    private String contactPerson;
    private String country;
    private String city;
    private String contactEmail;
    private String contactPhone;
    private int typeId;

    // Constructors, getters, and setters
}