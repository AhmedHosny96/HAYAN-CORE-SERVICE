package com.hayaan.flight.object.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Airlines")
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "AirLineName")
    private String airLineName;

    @Column(name = "AirLineCode")
    private String airLineCode;
}

