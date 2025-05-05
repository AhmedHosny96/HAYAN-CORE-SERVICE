package com.hayaan.flight.object.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "PaymentMethod")
@Data
public class PaymentMethod {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Name", nullable = false, unique = true)
    private String name;

    @Column(name = "Code", nullable = false, unique = true)
    private String code;

    @Column(name = "Logo")
    private String logo;

    @Column(name = "Status")
    private int status = 1;
}
