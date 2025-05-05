package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.TicketConversionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketConversionHistoryRepo extends JpaRepository<TicketConversionHistory, Long> {


}
