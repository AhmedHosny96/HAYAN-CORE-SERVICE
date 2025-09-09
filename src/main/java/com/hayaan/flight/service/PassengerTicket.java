package com.hayaan.flight.service;

import com.hayaan.flight.object.entity.Passenger;
import com.hayaan.flight.object.entity.TicketHistory;
import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "PassengerTicket")
@Data
public class PassengerTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PassengerId", nullable = false)
    private Passenger passenger;

    @ManyToOne
    @JoinColumn(name = "TicketId", nullable = false)
    private TicketHistory ticketHistory;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

