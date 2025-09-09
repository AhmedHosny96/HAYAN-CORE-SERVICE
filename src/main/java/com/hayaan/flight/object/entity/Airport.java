package com.hayaan.flight.object.entity;


import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Airport")
public class Airport {

    @javax.persistence.Id
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
