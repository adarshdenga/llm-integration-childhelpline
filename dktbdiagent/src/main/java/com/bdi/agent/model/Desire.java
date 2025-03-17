package com.bdi.agent.model;

import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table (name = "desire")
@AllArgsConstructor
public class Desire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="agent_id", nullable=false)
    private Agent agent;
    private String name;
    private String fullName;
    private Boolean isActive;

    @OneToMany(mappedBy = "desire", cascade = CascadeType.ALL)
    private Set<Action> actions;

    public Desire() {

    }

    public Desire(Agent agent, String name, String fullName, Boolean isActive) {
        this.agent = agent;
        this.name = name;
        this.fullName = fullName;
        this.isActive = isActive;
    }

    public Agent getAgent() {
        return agent;
    }

    public Long getId() {
        return id;
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

    public Boolean isActive() {
        return isActive;
    }

    public void setActiveValue(boolean value) {
        isActive = value;

    }

    public void setActive(Boolean aBoolean) {
        this.isActive = true;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


}
