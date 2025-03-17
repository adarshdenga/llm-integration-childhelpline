package com.bdi.agent.service;

import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.util.BeliefConstraint;
import com.bdi.agent.model.util.PhaseTransitionConstraints;
import com.bdi.agent.utils.ConstraintProvider;
import com.bdi.agent.utils.FloatComparer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConstraintService {

    private final List<BeliefName> beliefOrdering = Arrays.asList(BeliefName.values());
    private final List<Phase> phases = Arrays.asList(Phase.values());

    private final FloatComparer floatComparer;
    private final ConstraintProvider constraintProvider;

    /**
     * Creates a ConstraintService.
     *
     * @param floatComparer The floatComparer, used to safely compare float values.
     * @param constraintProvider The constraintProvider, used to get constraints for desires and phases.
     */
    @Autowired
    public ConstraintService(FloatComparer floatComparer, ConstraintProvider constraintProvider) {
        this.floatComparer = floatComparer;
        this.constraintProvider = constraintProvider;
    }

    /**
     * Checks the constraints for a given desire and the belief values (i.e., if that desire is active).
     *
     * @param desire    The desire to check.
     * @param beliefs   The belief values.
     * @return Whether the desire is active.
     */
    public boolean checkDesireConstraints(DesireName desire, float[] beliefs) {
        return areDnfConstraintsSatisfied(constraintProvider.getDesireConstraints(desire), beliefs);
    }

    /**
     * Returns the set of sets, in disjunctive normal form, which describes if it is impossible to reach
     * phase five from the current, given, one. If the constraints are satisfied, it is not possible.
     *
     * @param phaseFrom The phase from which the constraints should be generated.
     * @return  The set of sets, in disjunctive normal form, which describes if it is impossible to reach
     *          phase five from the current one.
     */
    public Set<Set<BeliefConstraint>> impossibleToReachPhaseFiveConstraints(Phase phaseFrom) {
        // If any of the sets of constraints is satisfied, the last phase cannot be reached.
        // Therefore, this is in disjunctive normal form. Since the constraints per phase are also in
        // DNF, we can flatMap the sets.

        return phases.subList(phases.indexOf(phaseFrom), phases.size() - 1)
                .stream().flatMap(phase -> {
                    PhaseTransitionConstraints constraints = constraintProvider.getPhaseTransitionConstraints(phase);

                    if (constraints != null) {
                        return constraints.getImpossibleToReach().stream();
                    }
                    return Stream.empty();
                }).collect(Collectors.toSet());
    }

    /**
     * Gets the phaseConstraintMap, mapping a phase to its constraints.
     *
     * @return the phaseConstraintMap, mapping a phase to its constraints.
     */
    public Map<Phase, PhaseTransitionConstraints> getPhaseConstraintMap() {
        Map<Phase, PhaseTransitionConstraints> phaseConstraintMap = new HashMap<>();
        for (Phase phase : Phase.values()) {
            phaseConstraintMap.put(phase, constraintProvider.getPhaseTransitionConstraints(phase));
        }
        return phaseConstraintMap;
    }

    /**
     * Gets the phaseImpossibleToReachEndMap, mapping the phase to the constraints making it impossible
     * to reach the last phase from that phase.
     *
     * @return  the phaseImpossibleToReachEndMap, mapping the phase to the constraints making it impossible
     *          to reach the last phase from that phase.
     */
    public Map<Phase, Set<Set<BeliefConstraint>>> getPhaseImpossibleToReachEndMap() {
        Map<Phase, Set<Set<BeliefConstraint>>> phaseImpossibleToReachEndMap = new HashMap<>();
        for (Phase phase : Phase.values()) {
            phaseImpossibleToReachEndMap.put(phase, impossibleToReachPhaseFiveConstraints(phase));
        }
        return phaseImpossibleToReachEndMap;
    }

    /**
     * Checks if a given constraint set of sets, in disjunctive normal form, is satisfied with the given
     * belief values.
     *
     * @param constraintSet     The set of constraint sets.
     * @param beliefs           The belief values.
     * @return  Whether the constraints in disjunctive normal form are satisfied, considering
     *          the belief values.
     */
    public boolean areDnfConstraintsSatisfied(Set<Set<BeliefConstraint>> constraintSet, float[] beliefs) {
        // The constraintSet is in disjunctive normal form, so a disjunction of conjunctions
        if (constraintSet.isEmpty()) {
            return true;
        }

        for (Set<BeliefConstraint> conjunction : constraintSet) {
            // In the conjunction, all constraints need to be satisfied for it to be true
            boolean isConjunctionSatisfied = true;

            for (BeliefConstraint constraint : conjunction) {
                if (!isConstraintSatisfied(constraint, beliefs)) {
                    isConjunctionSatisfied = false;
                    break;
                }
            }

            // If one of the conjunction evaluates to true, the disjunction is true
            if (isConjunctionSatisfied) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a given constraint set of sets, in conjunctive normal form, is satisfied with the given
     * belief values.
     *
     * @param constraintSet     The set of constraint sets.
     * @param beliefs           The belief values.
     * @return  Whether the constraints in conjunctive normal form are satisfied, considering
     *          the belief values.
     */
    public boolean areCnfConstraintsSatisfied(Set<Set<BeliefConstraint>> constraintSet, float[] beliefs) {
        // The constraintSet is in conjunctive normal form, so a conjunctive of disjunctions

        // In the conjunction, all constraints need to be satisfied for it to be true
        for (Set<BeliefConstraint> disjunction : constraintSet) {
            boolean isDisjunctionSatisfied = false;

            for (BeliefConstraint constraint : disjunction) {
                if (isConstraintSatisfied(constraint, beliefs)) {
                    isDisjunctionSatisfied = true;
                    break;
                }
            }

            // If one of the disjunctions evaluates to false, the conjunction is false
            if (!isDisjunctionSatisfied && !disjunction.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a single constraint is satisfied, given the belief values.
     *
     * @param constraint    The constraint to check.
     * @param beliefs       The belief values.
     * @return Whether the constraint is satisfied.
     */
    public boolean isConstraintSatisfied(BeliefConstraint constraint, float[] beliefs) {
        // If the belief name is not a valid one, returns false
        int idx = beliefOrdering.indexOf(constraint.getBeliefName());
        if (idx == -1 || idx >= beliefs.length) {
            return false;
        }

        // Check if the constraint is satisfied
        return switch (constraint.getBoundaryCheck()) {
            case GEQ -> floatComparer.greaterOrEqual(beliefs[idx], constraint.getGoalValue());
            case LEQ -> floatComparer.lessOrEqual(beliefs[idx], constraint.getGoalValue());
            case EQ -> floatComparer.equalTo(beliefs[idx], constraint.getGoalValue());
            case GT -> floatComparer.greaterThan(beliefs[idx], constraint.getGoalValue());
            case LT -> floatComparer.lessThan(beliefs[idx], constraint.getGoalValue());
            case NEQ -> !floatComparer.equalTo(beliefs[idx], constraint.getGoalValue());
        };
    }

    /**
     * Checks if a constraint set is empty (also checks inner set).
     *
     * @param constraintSet The constraint set to check.
     * @return If a constraint set is empty (also checks inner set).
     */
    public boolean isConstraintSetEmpty(Set<Set<BeliefConstraint>> constraintSet) {
        return constraintSet.isEmpty() || constraintSet.equals(Set.of(Set.of()));
    }
}
