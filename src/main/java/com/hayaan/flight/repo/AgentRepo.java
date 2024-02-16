package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepo extends JpaRepository<Agent, Long> {

    Optional<Agent> findByName(String name);
}
