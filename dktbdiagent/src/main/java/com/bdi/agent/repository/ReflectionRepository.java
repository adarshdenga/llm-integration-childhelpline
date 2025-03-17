package com.bdi.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bdi.agent.model.Reflection;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {
}
