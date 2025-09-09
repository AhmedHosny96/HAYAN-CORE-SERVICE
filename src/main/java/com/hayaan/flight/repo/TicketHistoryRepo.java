package com.hayaan.flight.repo;

import com.hayaan.auth.object.entity.User;
import com.hayaan.flight.object.entity.Agent;
import com.hayaan.flight.object.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketHistoryRepo extends JpaRepository<TicketHistory, Long> {


    Optional<TicketHistory> findByPnr(String pnr);

//    Optional<TicketHistory> findByStatus(Integer status);

    Optional<TicketHistory> findByPnrAndStatus(String pnr, int status);

    Optional<TicketHistory> findByPnrAndPtrUniqueID(String pnr, Long ptrUniqueId);

    List<TicketHistory> findByAgent(Agent agent);

    List<TicketHistory> findByUser(User user);

//    @Query("SELECT DISTINCT t FROM TicketHistory t JOIN t.passengers p WHERE p.id = :passengerId")
//    List<TicketHistory> findByPassengerId(@Param("passengerId") Long passengerId);


    List<TicketHistory> findAllByCreatedDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
