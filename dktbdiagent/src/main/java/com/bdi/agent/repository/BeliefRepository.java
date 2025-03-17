package com.bdi.agent.repository;

import com.bdi.agent.model.Belief;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface BeliefRepository extends JpaRepository<Belief, Long> {

    Belief findByAgentIdAndName(Long agentId, String name);

    Set<Belief> findByAgentIdOrderByPhaseAsc(Long agentId);

}
