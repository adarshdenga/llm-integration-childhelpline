package com.bdi.agent.utils;

import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BoundaryCheck;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.util.BeliefConstraint;
import com.bdi.agent.model.util.PhaseTransitionConstraints;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConstraintProvider {
    @Value("${oneStep}")
    private float oneStep;
    @Value("${minThreshold}")
    private float minThreshold;
    @Value("${midThreshold}")
    private float midThreshold;
    @Value("${maxThreshold}")
    private float maxThreshold;
    @Value("${minValue}")
    private float minValue;
    @Value("${maxValue}")
    private float maxValue;

    /**
     * Returns the belief constraints/conditions to make the given desire active in disjunctive normal form.
     *
     * @param desire The desire.
     * @return The belief constraints/conditions to make the given desire active in disjunctive normal form.
     */
    public Set<Set<BeliefConstraint>> getDesireConstraints(DesireName desire) {
        // TODO this could be changed to either load from a file, or be loaded to a database
// D1->D3->D5->D4->D6->D2
        return switch (desire) {
            case D1 -> Set.of(Set.of(
                    new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B10, minValue),
                    new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B12, minValue),
                    new BeliefConstraint(BoundaryCheck.LEQ, BeliefName.B9, maxThreshold),
                    new BeliefConstraint(BoundaryCheck.GT, BeliefName.B3, minThreshold),
                    new BeliefConstraint(BoundaryCheck.LT, BeliefName.B15, maxValue) // if they say bye they leave the conversation
            ));
            case D2 -> Set.of(
                    Set.of(new BeliefConstraint(BoundaryCheck.LT, BeliefName.B1, minThreshold)), // add this as a single constraint to another desire. now if activated there will be two true desires
                    Set.of(new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B17, maxValue)), // nothing is linked. b17 they think kt will solve for them
                    Set.of(new BeliefConstraint(BoundaryCheck.LT, BeliefName.B3, midThreshold),
                            new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B12, maxValue)), // asking about confidant without showing enough empathy
                    Set.of(new BeliefConstraint(BoundaryCheck.GT, BeliefName.B9, maxThreshold),
                            new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B10, minValue)), //in phase 2  the child says a lot about their situation with trigger
                    Set.of(new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B10, maxValue),
                            new BeliefConstraint(BoundaryCheck.LT, BeliefName.B8, maxValue),
                            new BeliefConstraint(BoundaryCheck.LT, BeliefName.B16, maxThreshold)), // saying no we can't do that when asked if they can call school
                    Set.of(new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B2, maxValue),
                            new BeliefConstraint(BoundaryCheck.LEQ, BeliefName.B13, midThreshold)), // if they are confident and feel they don't need the helpline anymore
                    Set.of(new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B15, maxValue)) // add this alone? if they say goodbye.
                             //or midthreeshhold? check!
            // make something for B7 and B8, if they say (no we can't do that when asked if they can call school)
            );
            case D3 -> Set.of(Set.of(
                    new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B10, maxValue),
                    new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B8, maxValue),
                    new BeliefConstraint(BoundaryCheck.LT, BeliefName.B2, maxThreshold)
            ));
            case D4 -> Set.of(
                    Set.of(
                    new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B12, maxValue),
                    new BeliefConstraint(BoundaryCheck.LEQ, BeliefName.B13, midThreshold),
                    new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B3, midThreshold),
                    new BeliefConstraint(BoundaryCheck.LT, BeliefName.B2, maxThreshold)
                    ),
                    Set.of(
                            new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B12, maxValue),
                            new BeliefConstraint(BoundaryCheck.LEQ, BeliefName.B13, midThreshold),
                            new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B3, midThreshold),
                            new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B2, maxThreshold),
                            new BeliefConstraint(BoundaryCheck.LT, BeliefName.B2, maxValue)
                    ),
                    Set.of(
                            new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B12, maxValue),
                            new BeliefConstraint(BoundaryCheck.GT, BeliefName.B13, midThreshold),
                            new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B3, midThreshold),
                            new BeliefConstraint(BoundaryCheck.LT, BeliefName.B2, maxThreshold)
                    )
            );
            case D5 -> Set.of(Set.of(
                    new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B4, midThreshold),
                    new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B10, maxValue),
                    new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B12, minValue),
                    new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B16, maxThreshold)
            ));
            case D6 -> Set.of(Set.of(
                    new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B12, maxValue),
                    new BeliefConstraint(BoundaryCheck.GT, BeliefName.B13, midThreshold),
                    new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B3, midThreshold),
                    new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B2, maxThreshold),
                    new BeliefConstraint(BoundaryCheck.LT, BeliefName.B15, maxValue)
            ));
        };
    }

    /**
     * Returns an object describing the transition constraints for the given phase. This includes, the phase
     * from which is generated, the phase to which is generated, which conditions/constraints would lead to the
     * phase not being reachable, and which beliefs should be influenced in which way, and when.
     *
     * @param phaseFrom The phase from which to generate/transition.
     * @return An object describing the transition constraints for the given phase.
     */
    // Suppressed to keep the structure similar per phase
    @SuppressWarnings("VariableDeclarationUsageDistance")
    public PhaseTransitionConstraints getPhaseTransitionConstraints(Phase phaseFrom) {
        // TODO this could be changed to either load from a file, or be loaded to a database

        return switch (phaseFrom) {
            case PHASE1 -> {
                // GOAL to reach phase 2: Activate D1
                //     => B3 > 0.3 && B9 < 0.8 && B10 = 0 && B12 = 0

                BeliefConstraint constraintB3 = new BeliefConstraint(BoundaryCheck.GT, BeliefName.B3, minThreshold);
                BeliefConstraint constraintB10 = new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B10, minValue);
                BeliefConstraint constraintB9 = new BeliefConstraint(BoundaryCheck.LEQ, BeliefName.B9, maxThreshold);
                BeliefConstraint constraintB12 = new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B12, minValue);

                Set<Set<BeliefConstraint>> impossibleToReach = new HashSet<>();
                // Cannot increase B9
                impossibleToReach.add(Set.of(reverseBeliefConstraint(constraintB9)));
                // Cannot increase B12
                impossibleToReach.add(Set.of(reverseBeliefConstraint(constraintB12)));

                Map<Set<BeliefConstraint>, BeliefConstraint> goalValues = new HashMap<>();
                // Need to increase B3
                goalValues.put(
                        Set.of(reverseBeliefConstraint(constraintB3)), constraintB3
                );
                // Need to decrease B10
                goalValues.put(
                        Set.of(reverseBeliefConstraint(constraintB10)), constraintB10
                );

                // B3 should always be average of B4 - B7
                float b3Value = (minValue * 2 + midThreshold + maxThreshold) / 4;
                // B3 > 0.3 && B9 < 0.8 && B10 = 0 && B12 = 0
                float[] exampleBeliefValues = new float[]{
                        midThreshold, minThreshold, b3Value, midThreshold, minValue,
                        minValue, maxThreshold, maxValue, minValue, minValue,
                        minValue, minValue, midThreshold, maxValue, minValue,
                        minValue, minValue};

                yield new PhaseTransitionConstraints(phaseFrom, Phase.PHASE2, goalValues, impossibleToReach, Set.of(),
                        exampleBeliefValues);
            }
            case PHASE2 -> {
                // GOAL to reach phase 3: Activate D3 OR D5
                //  - D3   => B2 < 0.7 && B08 > 0.7 && B10 = 1
                //  - D5   => B04 >= 0.5 && B10 = 1 && B12 = 0 && B16 >= 0.7



                BeliefConstraint constraintB2 = new BeliefConstraint(BoundaryCheck.LT, BeliefName.B2, maxThreshold);
                BeliefConstraint constraintB8 = new BeliefConstraint(BoundaryCheck.GT, BeliefName.B8, maxThreshold);
                BeliefConstraint constraintB10 = new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B10, maxValue);
                BeliefConstraint constraintB4 = new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B4, midThreshold);
                BeliefConstraint constraintB12 = new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B12, minValue);

                // Here D3 and D5 need to be considered:
                // - Cannot increase B2, or decrease B8, which are needed for D3
                // - Cannot decrease B12, which is needed for D5
                // We need DNF, so from the following CNF (B2 && B8) || B12, this gives (DeMorgan's law):
                // => (B12 && B2) || (B12 && B8)
                Set<Set<BeliefConstraint>> impossibleToReach = new HashSet<>();
                impossibleToReach.add(
                        Set.of(reverseBeliefConstraint(constraintB12), reverseBeliefConstraint(constraintB2))
                );
                impossibleToReach.add(
                        Set.of(reverseBeliefConstraint(constraintB12), reverseBeliefConstraint(constraintB8))
                );

                Map<Set<BeliefConstraint>, BeliefConstraint> goalValues = new HashMap<>();
                // Need to increase B4 for D5
                // Condition to increase B4 is that D5 can be activated (B12 is correct value),
                // and B4 is not yet a correct value
                goalValues.put(
                        Set.of(reverseBeliefConstraint(constraintB12), reverseBeliefConstraint(constraintB4)),
                        constraintB4
                );
                // Need to increase B10 for both D3 and D5
                goalValues.put(
                        Set.of(reverseBeliefConstraint(constraintB10)), constraintB10
                );

                float b3Value = (minThreshold * 2 + maxThreshold * 2) / 4;
                float[] exampleBeliefValues = new float[]{
                        midThreshold, minThreshold, b3Value, maxThreshold, 0,
                        0, maxThreshold, maxValue, minValue, minValue,
                        minValue, minValue, midThreshold, maxValue, minValue,
                        minValue, minValue};

                yield new PhaseTransitionConstraints(phaseFrom, Phase.PHASE3, goalValues, impossibleToReach, Set.of(),
                        exampleBeliefValues);
            }
            case PHASE3 -> {
                // GOAL to reach phase 4: Activate D4
                //      => B03 >= 0.5 && B12 = 1 && B13 >= 0.5

                BeliefConstraint constraintB3 = new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B3, midThreshold);
                BeliefConstraint constraintB12 = new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B12, maxValue);
                BeliefConstraint constraintB13 = new BeliefConstraint(BoundaryCheck.GEQ, BeliefName.B13, midThreshold);

                // It is always possible to go to phase 4
                Set<Set<BeliefConstraint>> impossibleToReach = new HashSet<>();

                Map<Set<BeliefConstraint>, BeliefConstraint> goalValues = new HashMap<>();
                // Need to increase B3
                goalValues.put(
                        Set.of(reverseBeliefConstraint(constraintB3)), constraintB3
                );
                // Need to increase B12
                goalValues.put(
                        Set.of(reverseBeliefConstraint(constraintB12)), constraintB12
                );
                // Need to increase B13
                goalValues.put(
                        Set.of(reverseBeliefConstraint(constraintB13)), constraintB13
                );

                float b3Value = (minThreshold * 2 + maxThreshold + (maxValue - oneStep)) / 4;
                float[] exampleBeliefValues = new float[]{
                        midThreshold, minThreshold, b3Value, maxThreshold, minThreshold,
                        minThreshold, (maxValue - oneStep), maxValue, minValue, maxValue,
                        minValue, minValue, midThreshold, maxValue, minValue,
                        minValue, minValue};

                yield new PhaseTransitionConstraints(phaseFrom, Phase.PHASE4, goalValues, impossibleToReach, Set.of(),
                        exampleBeliefValues);
            }
            case PHASE4 -> {
                // GOAL to reach phase 5: Activate D2
                //      => B01 < 0.3 || B17 = 1 || (B3 < 0.5 && B12 = 1) || B2 = 1
                //      => B2: Competent to solve the problem => This is the optimal outcome

                BeliefConstraint constraintB2 = new BeliefConstraint(BoundaryCheck.EQ, BeliefName.B2, maxValue);

                // It is always possible to go to phase 5, so try to reach D2
                // => Only by the means of B2
                Set<Set<BeliefConstraint>> impossibleToReach = new HashSet<>();

                Map<Set<BeliefConstraint>, BeliefConstraint> goalValues = new HashMap<>();
                // Need to increase B2
                goalValues.put(
                        Set.of(reverseBeliefConstraint(constraintB2)), constraintB2
                );

                float b3Value = (minThreshold * 2 + maxThreshold + maxValue) / 4;
                float[] exampleBeliefValues = new float[]{
                        midThreshold, minThreshold, b3Value, maxThreshold, minThreshold,
                        minThreshold, maxValue, minValue, minValue, maxValue,
                        minValue, maxValue, midThreshold, maxValue, minValue,
                        maxValue, minValue};
                
                // The last phase is reached optimally by means of B2
                Set<Set<BeliefConstraint>> optimalGoal = Set.of(Set.of(constraintB2));
                
                yield new PhaseTransitionConstraints(phaseFrom, Phase.PHASE5, goalValues, impossibleToReach,
                        optimalGoal, exampleBeliefValues);
            }
            case PHASE5 -> {
                // From phase 5, there is no next phase to reach anymore, we still have belief values to set the
                // agent to be in this phase

                float b3Value = (minThreshold * 2 + maxThreshold + maxValue) / 4;
                float[] exampleBeliefValues = new float[]{
                        midThreshold, maxValue, b3Value, maxThreshold, minThreshold,
                        minThreshold, maxValue, minValue, minValue, maxValue,
                        minValue, maxValue, midThreshold, maxValue, minValue,
                        minValue, minValue};

                yield new PhaseTransitionConstraints(phaseFrom, null, null, null,
                        null, exampleBeliefValues);
            }
        };
    }

    /**
     * Reverses the boundary of a given constraint, for example changes <= to >.
     *
     * @param constraint The constraint to reverse.
     * @return The reversed constraint (i.e., with reversed boundary).
     */
    public BeliefConstraint reverseBeliefConstraint(BeliefConstraint constraint) {
        Map<BoundaryCheck, BoundaryCheck> reversedBoundary = Map.of(
                BoundaryCheck.EQ, BoundaryCheck.NEQ,
                BoundaryCheck.NEQ, BoundaryCheck.EQ,
                BoundaryCheck.LEQ, BoundaryCheck.GT,
                BoundaryCheck.GEQ, BoundaryCheck.LT,
                BoundaryCheck.LT, BoundaryCheck.GEQ,
                BoundaryCheck.GT, BoundaryCheck.LEQ);

        return new BeliefConstraint(reversedBoundary.get(constraint.getBoundaryCheck()),
                constraint.getBeliefName(), constraint.getGoalValue());
    }
}
