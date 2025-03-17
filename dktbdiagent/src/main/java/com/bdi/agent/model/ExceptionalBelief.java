package com.bdi.agent.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExceptionalBelief extends Belief {

    private String exceptionalReason;

    public ExceptionalBelief(Agent agent, String name, String fullName, String phase, Float value,
                             String exceptionalReason) {
        super(agent, name, fullName, phase, value);
        this.exceptionalReason = exceptionalReason;
    }

    public ExceptionalBelief(String name, String fullName, Float value, String exceptionalReason) {
        super(name, fullName, value);
        this.exceptionalReason = exceptionalReason;
    }
}
