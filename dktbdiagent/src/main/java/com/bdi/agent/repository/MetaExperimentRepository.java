package com.bdi.agent.repository;

import com.bdi.agent.model.MetaExperiment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaExperimentRepository extends JpaRepository<MetaExperiment, Long> {
    MetaExperiment findByUsername(String username);


}
