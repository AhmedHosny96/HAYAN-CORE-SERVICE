package com.hayaan.flight.object.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Airports")
public class Airport {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @Column(name = "AirportCode")
    private String airportCode;

    @Column(name = "AirportName")
    private String airportName;

    @Column(name = "Country")
    private String country;

    @Column(name = "City")
    private String city;


}
