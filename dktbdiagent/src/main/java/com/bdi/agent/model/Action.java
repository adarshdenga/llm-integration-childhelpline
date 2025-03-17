package com.bdi.agent.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table (name = "action")
@Getter
@Setter
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade =  CascadeType.ALL)
    @JoinColumn(name="desire_id", referencedColumnName = "id")
    private Desire desire;

    private String type;
    private String name;
    private String subject;
    private String attribute;
    private Boolean completed;

    public Action() {

    }

    public Action(Desire desire, String type, String name, String subject, String attribute, Boolean isCompleted) {
        this.desire = desire;
        this.type = type;
        this.name = name;
        this.subject = subject;
        this.attribute = attribute;
        this.completed = isCompleted;
    }
}
