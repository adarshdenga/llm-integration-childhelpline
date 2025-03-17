package com.bdi.agent.model.util;

import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BoundaryCheck;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BeliefConstraint {
    private BoundaryCheck boundaryCheck;
    private BeliefName beliefName;
    private float goalValue;
}
