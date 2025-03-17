package com.bdi.agent.repository;

import com.bdi.agent.model.Desire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesireRepository extends JpaRepository<Desire, Long> {

    Desire findByAgentIdAndName(Long agentId, String name);

    List<Desire> findByAgentIdOrderByName(Long agentId);

}
