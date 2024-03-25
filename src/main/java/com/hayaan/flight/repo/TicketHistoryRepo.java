package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketHistoryRepo extends JpaRepository<TicketHistory, Long> {


    Optional<TicketHistory> findByPnr(String pnr);
}
