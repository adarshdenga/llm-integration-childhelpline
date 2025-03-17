package com.bdi.agent.model.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitialBeliefModel {

    @NotBlank
    private String id;

    private float value;

    @NotNull
    private String fullName;

    @NotNull
    private Boolean isModifiable = true;

    private String disableReason;

    /**
     * Constructor for non-exceptional beliefs.
     *
     * @param id the Belief id (so the name, e.g. 'B2')
     * @param value the Belief value
     * @param fullName the Belief's fullName, which is a sentence
     */
    public InitialBeliefModel(String id, float value, String fullName) {
        this.id = id;
        this.value = value;
        this.fullName = fullName;
    }
}
