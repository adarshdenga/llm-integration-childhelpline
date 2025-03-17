package com.bdi.agent.model.util;

import com.bdi.agent.model.enums.Phase;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class describing constraints for reaching the next phase from the current one.
 */
@Data
@AllArgsConstructor
public class PhaseTransitionConstraints {
    private Phase currentPhase;
    private Phase nextPhase;

    // If the set of constraints (conjunction) is satisfied, then try to reach the mapped to constraint
    // if it is not yet satisfied
    private Map<Set<BeliefConstraint>, BeliefConstraint> goalValues;

    // If these constraints (disjunctive normal form) are satisfied, it
    // is impossible to reach the next phase
    private Set<Set<BeliefConstraint>> impossibleToReach;

    // One of the goal constraints (disjunctive normal form) may be the only one which should be reached
    // This attribute specifies the *additional* constraints for reaching the next phase optimally
    private Set<Set<BeliefConstraint>> optimalGoal;

    // An example float array of belief values to jump to this phase (not the next one!)
    private float[] exampleBeliefValues;
}
