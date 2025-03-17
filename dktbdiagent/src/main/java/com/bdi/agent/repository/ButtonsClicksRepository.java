package com.bdi.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bdi.agent.model.ButtonsClicks;

public interface ButtonsClicksRepository extends JpaRepository<ButtonsClicks, Long> {
}
