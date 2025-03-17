package com.bdi.agent.repository;

import com.bdi.agent.model.Knowledge;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {

    Knowledge findByKnowledgeAndSubjectAndAttribute(String knowledge, String subject, String attribute);

    @Query("SELECT DISTINCT k.knowledge FROM Knowledge k")
    List<String> findAllUniqueKnowledgeFiles();
}
