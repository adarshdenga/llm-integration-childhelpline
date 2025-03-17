package com.bdi.agent.model.api;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BeliefChangeModel {

    @NotBlank
    private String belief;

    private float value;

}
