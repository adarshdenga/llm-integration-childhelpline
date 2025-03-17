package com.bdi.agent.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "reflections")
public class Reflection {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(length = 5000)
    private String conversationReflection;

    @Column(length = 5000)
    private String learningReflection;

    private Timestamp submissionTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConversationReflection() {
        return conversationReflection;
    }

    public void setConversationReflection(String conversationReflection) {
        this.conversationReflection = conversationReflection;
    }

    public String getLearningReflection() {
        return learningReflection;
    }

    public void setLearningReflection(String learningReflection) {
        this.learningReflection = learningReflection;
    }

    public Timestamp getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Timestamp submissionTime) {
        this.submissionTime = submissionTime;
    }
}
