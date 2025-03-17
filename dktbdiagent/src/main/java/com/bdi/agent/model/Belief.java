package com.bdi.agent.model;

import lombok.AllArgsConstructor;

import javax.persistence.*;
import lombok.EqualsAndHashCode;

@Entity
@Table (name = "belief")
@AllArgsConstructor
@EqualsAndHashCode
public class Belief {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade =  CascadeType.ALL)
    @JoinColumn(name="agent_id", referencedColumnName = "id")
    private Agent agent;

    private String name;
    private String fullName;
    private String phase;
    private Float value;

    public Belief() {

    }

    public Belief(Agent agent, String name, String fullName, String phase, Float value) {
        this.agent = agent;
        this.name = name;
        this.fullName = fullName;
        this.phase = phase;
        this.value = value;
    }

    public Belief(String name, String fullName, Float value) {
        this.name = name;
        this.fullName = fullName;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
