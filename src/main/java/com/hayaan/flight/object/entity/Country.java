package com.hayaan.flight.object.entity;


import javax.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "Country")
public class Country {

    @Id
    @Column(name = "Id")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "CountryCode")
    private String code;

    @Column(name = "DialingCode")
    private String dialingCode;


}
