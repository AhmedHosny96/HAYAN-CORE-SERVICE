package com.hayaan.flight.repo;

import com.hayaan.flight.object.entity.AgentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentTypeRepo extends JpaRepository<AgentType, Integer> {

    Optional<AgentType> findByType(String type);
}
