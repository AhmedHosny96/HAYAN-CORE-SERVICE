package com.hayaan.flight.object.entity;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Airline")
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "AirLineName")
    private String airLineName;

    @Column(name = "AirLineCode")
    private String airLineCode;
    @Column(name = "AirLineLogo")
    private String airLineLogo;
}

