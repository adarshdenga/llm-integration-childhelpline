package com.bdi.agent.model.api;

import com.bdi.agent.model.enums.Phase;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PhaseChangeResponse {
    // Can be null, if the phase was not set before
    private Phase phaseFrom;
    @NotNull
    private Phase phaseTo;
}
