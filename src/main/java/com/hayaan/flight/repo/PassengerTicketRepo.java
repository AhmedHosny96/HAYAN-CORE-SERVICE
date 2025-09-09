package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.Passenger;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.service.PassengerTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerTicketRepo extends JpaRepository<PassengerTicket,Long> {

    List<PassengerTicket> findByTicketHistory(TicketHistory ticketHistory);
    List<PassengerTicket> findByPassenger(Passenger passenger);
}
