package com.bdi.agent.service.graph;

import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BoundaryCheck;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.graph.GraphEdge;
import com.bdi.agent.model.graph.GraphNode;
import com.bdi.agent.model.util.PhaseTransitionConstraints;
import com.bdi.agent.service.ConstraintService;
import com.bdi.agent.utils.FloatComparer;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GraphUpdatingService {
    @Value("${oneStep}")
    private float oneStep;
    @Value("${twoSteps}")
    private float twoSteps;
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

    private final FloatComparer floatComparer;
    private final GraphUtilsService graphUtils;
    private final ConstraintService constraintService;

    private final List<String> subjectBullying = List.of("request_bullying_who", "request_bullying_details",
            "request_bullying_count", "request_bullying_location", "request_bullying_duration",
            "request_bullying_frequency", "request_bullying_why", "request_bullying_response",
            "request_bullying_confidant", "request_bullying_parent", "confirm_bullying_summary");

    private final List<String> subjectGoal = List.of("inform_goal_negative", "inform_goal_help",
            "request_goal_what", "request_goal_dream", "request_goal_feeling", "confirm_goal_summary",
            "confirm_goal_collaborate", "request_goal_howkt", "request_goal_howchild");

    private final List<String> subjectConfidant = List.of("request_confidant_who", "confirm_confidant_teacher",
            "confirm_confidant_parent", "request_confidant_when", "request_confidant_feeling",
            "request_confidant_why", "request_confidant_how", "inform_confidant_help", "inform_confidant_say",
            "request_confidant_say", "confirm_confidant_summary");

    /**
     * Creates a GraphUpdatingService.
     *
     * @param floatComparer         The floatComparer, used to safely compare float values.
     * @param graphUtils            The beliefDesireService, used to access/update beliefs and desires. Also
     *                              contains utility methods.
     * @param constraintService     The constraintService, used to check and handle constraints.
     */
    @Autowired
    public GraphUpdatingService(FloatComparer floatComparer, GraphUtilsService graphUtils,
                                ConstraintService constraintService) {
        this.floatComparer = floatComparer;
        this.graphUtils = graphUtils;
        this.constraintService = constraintService;
    }

    /**
     * Try to increase/decrease/set a given belief, to reach a new phase. The ModifyBelief object indicates in which
     * direction the modification is intended, which are the valid phases to transition to, and the value if the belief
     * should be set to one.
     *
     * @param belief        The belief to modify.
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateBelief(BeliefName belief, GraphNode current, float goalValue, BoundaryCheck boundary,
                                        PhaseTransitionConstraints constraints) {
        return switch (belief) {
            case B1 -> updateB1(current, goalValue, boundary, constraints);
            case B2 -> updateB2(current, goalValue, boundary, constraints);
            case B3 -> updateB3(current, goalValue, boundary, constraints);
            case B4 -> updateB4(current, goalValue, boundary, constraints);
            case B5 -> updateB5(current, goalValue, boundary, constraints);
            case B6 -> updateB6(current, goalValue, boundary, constraints);
            case B7 -> updateB7(current, goalValue, boundary, constraints);
            case B8 -> updateB8(current, goalValue, boundary, constraints);
            case B10 -> updateB10(current, goalValue, boundary, constraints);
            case B11 -> updateB11(current, goalValue, boundary, constraints);
            case B12 -> updateB12(current, goalValue, boundary, constraints);
            case B13 -> updateB13(current, goalValue, boundary, constraints);
            case B15 -> updateB15(current, goalValue, boundary, constraints);
            case B16 -> updateB16(current, goalValue, boundary, constraints);
            case B17 -> updateB17(current, goalValue, boundary, constraints);
            case B9, B14 -> new ArrayList<>(); // These beliefs are not/cannot be updated
        };
    }

    /**
     * Try to increase/decrease/set B1, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB1(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B1, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            updateNodeByIntentName(nodes, current, "request_goal_effect",
                    constraints);
            updateNodeByIntentName(nodes, current, "request_goal_feeling",
                    constraints);
            updateNodeByIntentName(nodes, current, "request_goal_howchild",
                    constraints);

            // If current subject (subject of last edge) was "goal", "request_unknown_feeling"
            // is also an intent which can be used to reach the goal
            if (current.getCurrentSubject() != null && current.getCurrentSubject().equals("goal")) {
                updateNodeByIntentName(nodes, current, "request_unknown_feeling",
                        constraints);
            }
        }
        // Currently, B1 cannot be decreased

        return nodes;
    }

    /**
     * Try to increase/decrease/set B2, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB2(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B2, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            // Only if current subject (subject of last edge) was "confidant" or "goal",
            // "ack_unknown_compliment" is an intent which can be used to reach the goal
            // "ack_goal_compliment", "ack_confidant_compliment" by themselves are not valid intents in the nlu
            if (current.getCurrentSubject() != null && (current.getCurrentSubject().equals("goal")
                    || current.getCurrentSubject().equals("confidant"))) {
                updateNodeByIntentName(nodes, current, "ack_unknown_compliment",
                        constraints);
            } else {
                // Set the subject to "goal"
                for (String intent : subjectGoal) {
                    boolean condition = current.getPhase().equals(Phase.PHASE4)
                            && (intent.equals("inform_goal_help") || intent.equals("confirm_goal_collaborate"));

                    if (!condition) {
                        updateNodeByIntentNameIntermediary(nodes, current, intent,
                                "ack_unknown_compliment", constraints);
                    }
                }

                // Set the subject to "confidant"
                for (String intent : subjectConfidant) {
                    boolean condition = current.getPhase().equals(Phase.PHASE4)
                            && intent.equals("request_confidant_who") && floatComparer.lessThan(
                            graphUtils.getBeliefValue(BeliefName.B3, current.getBeliefs()), midThreshold);

                    if (!condition) {
                        updateNodeByIntentNameIntermediary(nodes, current, intent,
                                "ack_unknown_compliment", constraints);
                    }
                }

            }

            // ---- Note for future adaptations: ----
            // This belief is currently only used in phase 4 to get to B2 = 1.
            // If the current subject is neither "goal" nor "confidant", the subject should be set to
            // one of these. There are some restrictions to which intents are considered:
            //      1. inform_goal_help, confirm_goal_collaborate: This increases B7, and would lead to reaching
            //          phase 5, but not with the intended ending (B2 = 1).
            //      2. request_confidant_who: Increases B12, and if B3 < 0.5, this would lead to reaching
            //          phase 5, but not with the intended ending (B2 = 1).
            // If edits to the code are made, the implications on this need to be considered.
        }
        // Currently, B2 cannot be decreased

        return nodes;
    }

    /**
     * Try to increase/decrease/set B3, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB3(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B3, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else {
            // If it should increase, the goal value is 1/max value, since it is the maximal value
            // If it should decrease, the goal value is 0/min value, since it is the minimal value
            float innerGoalValue = graphUtils.needToIncrease(currentValue, goalValue, boundary)
                    ? maxValue : minValue;
            BoundaryCheck innerBoundary = graphUtils.needToIncrease(currentValue, goalValue, boundary)
                    ? BoundaryCheck.GEQ : BoundaryCheck.LEQ;

            // Try to in/decrease the value, or to set to a higher/lower value

            // Extra condition for B4, with the current implementation "request_chitchat_greeting" and
            // "request_chitchat_faring" only make sense in phase 1, and if it is the first node in the graph
            if (current.getPhase() == Phase.PHASE1 && current.getEdgeTo() == null) {
                nodes.addAll(updateB4(current, innerGoalValue, innerBoundary, constraints));
            }
            nodes.addAll(updateB5(current, innerGoalValue, innerBoundary, constraints));
            nodes.addAll(updateB6(current, innerGoalValue, innerBoundary, constraints));
            nodes.addAll(updateB7(current, innerGoalValue, innerBoundary, constraints));
        }

        return nodes;
    }

    /**
     * Try to increase/decrease/set B4, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB4(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B4, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            updateNodeByIntentName(nodes, current, "request_chitchat_greeting",
                    constraints);
            updateNodeByIntentName(nodes, current, "request_chitchat_faring",
                    constraints);
        }
        // Currently, B4 cannot be decreased

        return nodes;
    }

    /**
     * Try to increase/decrease/set B5, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB5(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B5, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            // Only if current subject (subject of last edge) was "bullying" or "goal",
            // "ack_unknown_empathize" is an intent which can be used to reach the goal
            // "ack_bullying_empathize", "ack_goal_empathize" by themselves are not valid intents in the nlu
            if (current.getCurrentSubject() != null && (current.getCurrentSubject().equals("goal")
                    || current.getCurrentSubject().equals("bullying"))) {
                updateNodeByIntentName(nodes, current, "ack_unknown_empathize",
                        constraints);
            } else {
                // Set the subject to "bullying"

                // NOTE: This is a hardcoded way of dealing with the graph generation taking an indefinite amount of
                // time if B3 <= 0.32. If the phase constraints are changed, this probably needs to be adjusted. The
                // first intent in the subjectBullying list, does not hinder the next phases, so it can be taken as an
                // "example" intent for increasing the value. The other intents would work similarly here, this is to
                // reduce the search space.
                if (floatComparer.lessOrEqual(graphUtils.getBeliefValue(BeliefName.B3,
                        current.getBeliefs()), 0.32f)) {
                    updateNodeByIntentNameIntermediary(nodes, current, subjectBullying.get(0),
                            "ack_unknown_empathize", constraints);
                } else {
                    for (String intent : subjectBullying) {
                        updateNodeByIntentNameIntermediary(nodes, current, intent, "ack_unknown_empathize",
                                constraints);
                    }
                }
            }

            // ---- Note for future adaptations: ----
            // This belief is currently only used in phase 1 indirectly by B3. The intents which set the subject to
            // "bullying" only increase B6 and decrease B10, which are valid and helpful settings for phase 1.
            // So if the subject is not the correct one, all intents which can be used to set it, are considered.
            // If edits to the code are made, the implications on this need to be considered.
        }
        // Currently, B5 cannot be decreased

        return nodes;
    }

    /**
     * Try to increase/decrease/set B6, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB6(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B6, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            // NOTE: This is a hardcoded way of dealing with the graph generation taking an indefinite amount of
            // time if B3 <= 0.32. If the phase constraints are changed, this probably needs to be adjusted. The first
            // intent in the subjectBullying list, does not hinder the next phases, so it can be taken as an "example"
            // intent for increasing the value. The other intents would work similarly here (apart from
            // confirm_bullying_summary), this is to reduce the search space.
            if (floatComparer.lessOrEqual(graphUtils.getBeliefValue(BeliefName.B3,
                    current.getBeliefs()), 0.32f)) {
                updateNodeByIntentNameIntermediary(nodes, current, subjectBullying.get(0),
                        "ack_unknown_empathize", constraints);
            } else {
                for (String intent : subjectBullying) {
                    if (!intent.equals("confirm_bullying_summary")) {
                        updateNodeByIntentName(nodes, current, intent, constraints);
                    }
                }
            }
        }
        // Currently, B6 cannot be decreased

        return nodes;
    }

    /**
     * Try to increase/decrease/set B7, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB7(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B7, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            updateNodeByIntentName(nodes, current, "ack_contactingkt_compliment",
                    constraints);
            updateNodeByIntentName(nodes, current, "inform_goal_help",
                    constraints);

            if (floatComparer.greaterThan(
                    graphUtils.getBeliefValue(BeliefName.B4, current.getBeliefs()), 0.5f)) {
                // This intent only increases B7, if B4 > 0.5
                updateNodeByIntentName(nodes, current, "confirm_goal_collaborate",
                         constraints);
            } else {
                // Try to increase B4
                nodes.addAll(updateB4(current, 0.5f, BoundaryCheck.GT, constraints));
            }
        } else {
            if (floatComparer.equalTo(
                    graphUtils.getBeliefValue(BeliefName.B10, current.getBeliefs()), maxValue)) {
                updateNodeByIntentName(nodes, current, "inform_goal_negative",
                        constraints);

                // If current subject (subject of last edge) was "goal", "inform_unknown_negative"
                // is also an intent which can be used to reach the goal
                if (current.getCurrentSubject() != null && current.getCurrentSubject().equals("goal")) {
                    updateNodeByIntentName(nodes, current, "inform_unknown_negative",
                            constraints);
                }
            } else {
                nodes.addAll(updateB10(current, maxValue, BoundaryCheck.GEQ, constraints));
            }
        }

        return nodes;
    }

    /**
     * Try to increase/decrease/set B8, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB8(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B8, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value
            return nodes;
        } else {
            // Try to decrease the value, or to set to a lower value

            if (floatComparer.greaterThan(
                    graphUtils.getBeliefValue(BeliefName.B4, current.getBeliefs()), 0.5f)) {
                // This intent only increases B8, if B4 > 0.5
                updateNodeByIntentName(nodes, current, "confirm_goal_collaborate",
                        constraints);
            } else {
                // Try to increase B4

                nodes.addAll(updateB4(current, 0.5f, BoundaryCheck.GT, constraints));
            }

            if (floatComparer.equalTo(
                    graphUtils.getBeliefValue(BeliefName.B10, current.getBeliefs()), maxValue)) {
                // This intent only sets B8, if B10 = maxValue
                updateNodeByIntentName(nodes, current, "inform_goal_negative",
                        constraints);

                // If current subject (subject of last edge) was "goal", "inform_unknown_negative"
                // is also an intent which can be used to reach the goal
                if (current.getCurrentSubject() != null && current.getCurrentSubject().equals("goal")) {
                    updateNodeByIntentName(nodes, current, "inform_unknown_negative",
                            constraints);
                }
            } else {
                // Try to set B10 to maxValue

                nodes.addAll(updateB10(current, maxValue, BoundaryCheck.GEQ, constraints));
            }

            if (floatComparer.greaterThan(
                    graphUtils.getBeliefValue(BeliefName.B13, current.getBeliefs()), 0.5f)) {
                // This intent only sets B8, if B13 > 0.5
                updateNodeByIntentName(nodes, current, "confirm_confidant_teacher",
                        constraints);
            } else {
                // Try to increase B13

                nodes.addAll(updateB13(current, 0.5f, BoundaryCheck.GT, constraints));
            }
        }

        return nodes;
    }

    /**
     * Try to increase/decrease/set B10, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB10(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B10, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Currently, B10 can only be increased if set to maxValue

            for (String goalIntent : subjectGoal) {
                updateNodeByIntentName(nodes, current, goalIntent, constraints);
            }

            // If current subject (subject of last edge) was "goal", "inform_unknown_negative"
            // is also an intent which can be used to reach the goal
            if (current.getCurrentSubject() != null && current.getCurrentSubject().equals("goal")) {
                updateNodeByIntentName(nodes, current, "inform_unknown_negative",
                        constraints);
            }
        } else {
            // Currently, B10 can only be decreased if set to minValue

            for (String intent : subjectBullying) {
                updateNodeByIntentName(nodes, current, intent, constraints);
            }
        }

        return nodes;
    }

    /**
     * Try to increase/decrease/set B11, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB11(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B11, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            // Increase
            updateNodeByIntentName(nodes, current, "request_goal_effect",
                    constraints);
            updateNodeByIntentName(nodes, current, "request_goal_feeling",
                    constraints);
            updateNodeByIntentName(nodes, current, "request_goal_howchild",
                    constraints);

            // If current subject (subject of last edge) was "goal", "request_unknown_feeling"
            // is also an intent which can be used to reach the goal
            if (current.getCurrentSubject() != null && current.getCurrentSubject().equals("goal")) {
                updateNodeByIntentName(nodes, current, "request_unknown_feeling", constraints);
            }

            // Set to maxValue
            updateNodeByIntentName(nodes, current, "request_goal_dream",
                    constraints);
        }
        // B11 can currently not be decreased

        return nodes;
    }

    /**
     * Try to increase/decrease/set B12, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB12(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B12, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            // Set to maxValue
            updateNodeByIntentName(nodes, current, "request_confidant_who",
                    constraints);

            // If current subject (subject of last edge) was confidant, "request_unknown_who"
            // is also an intent which can be used to reach the goal
            if (current.getCurrentSubject() != null && current.getCurrentSubject().equals("confidant")) {
                updateNodeByIntentName(nodes, current, "request_unknown_who",
                        constraints);
            }
        }
        // B12 can currently not be decreased

        return nodes;
    }

    /**
     * Try to increase/decrease/set B13, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB13(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B13, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            // Increase
            updateNodeByIntentName(nodes, current, "request_goal_effect",
                    constraints);
            updateNodeByIntentName(nodes, current, "request_goal_feeling",
                    constraints);
            updateNodeByIntentName(nodes, current, "request_goal_howchild",
                    constraints);

            // If current subject (subject of last edge) was "goal", "request_unknown_feeling"
            // is also an intent which can be used to reach the goal
            if (current.getCurrentSubject() != null && current.getCurrentSubject().equals("goal")) {
                updateNodeByIntentName(nodes, current, "request_unknown_feeling",
                        constraints);
            }

            // Set to maxValue
            updateNodeByIntentName(nodes, current, "inform_confidant_help",
                    constraints);
            updateNodeByIntentName(nodes, current, "inform_confidant_say",
                    constraints);
        }
        // B13 can currently not be decreased

        return nodes;
    }

    /**
     * Try to increase/decrease/set B15, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB15(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B15, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            // Set to maxValue
            updateNodeByIntentName(nodes, current, "request_chitchat_goodbye",
                    constraints);
        }
        // B14 can currently not be decreased

        return nodes;
    }

    /**
     * Try to increase/decrease/set B16, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB16(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B16, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            // Set to maxValue
            if (floatComparer.greaterThan(
                    graphUtils.getBeliefValue(BeliefName.B4, current.getBeliefs()), 0.5f)) {
                // This intent only sets B16, if B4 > 0.5
                updateNodeByIntentName(nodes, current, "confirm_goal_collaborate",
                        constraints);
            } else {
                // Try to increase B4

                nodes.addAll(updateB4(current, 0.5f, BoundaryCheck.GT, constraints));
            }

            if (floatComparer.greaterThan(
                    graphUtils.getBeliefValue(BeliefName.B13, current.getBeliefs()), 0.5f)) {
                // This intent only sets B16, if B13 > 0.5
                updateNodeByIntentName(nodes, current, "confirm_confidant_teacher",
                        constraints);
            } else {
                // Try to increase B13

                nodes.addAll(updateB13(current, 0.5f, BoundaryCheck.GT, constraints));
            }
        }
        // B16 can currently not be decreased

        return nodes;
    }

    /**
     * Try to increase/decrease/set B17, to reach a new phase. The goalValue indicates the value which is
     * the goal for this belief. The BoundaryCheck enum denotes whether the goal is <=, >=, < or >.
     * Also, the valid phases to transition to are considered.
     *
     * @param current       The current node.
     * @param goalValue     The value which is the goal for this belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @param constraints   The constraints for the current phase.
     * @return The list of generated nodes from the current one.
     */
    public List<GraphNode> updateB17(GraphNode current, float goalValue, BoundaryCheck boundary,
                                    PhaseTransitionConstraints constraints) {
        float currentValue = graphUtils.getBeliefValue(BeliefName.B17, current.getBeliefs());

        List<GraphNode> nodes = new ArrayList<>();

        if (graphUtils.stopUpdating(currentValue, goalValue, boundary)) {
            return nodes;
        } else if (graphUtils.needToIncrease(currentValue, goalValue, boundary))  {
            // Try to increase the value, or to set to a higher value

            // Set to maxValue
            if (floatComparer.equalTo(
                    graphUtils.getBeliefValue(BeliefName.B10, current.getBeliefs()), maxValue)) {
                // This intent only sets B17, if B10 = 1

                // Only if current subject (subject of last edge) was "goal", "inform_unknown_positive"
                // is an intent which can be used to reach the goal
                // "inform_goal_positive" by itself is not a valid intent in the nlu
                if (current.getCurrentSubject() != null && current.getCurrentSubject().equals("goal")) {
                    updateNodeByIntentName(nodes, current, "inform_unknown_positive",
                            constraints);
                } else {
                    // Set the subject to "goal"

                    for (String intent : subjectGoal) {
                        updateNodeByIntentNameIntermediary(nodes, current, intent,
                                "inform_unknown_positive", constraints);
                    }
                }

                // ---- Note for future adaptations: ----
                // This belief is currently not used, so if the subject is not the correct one,
                // all intents which can be used to set it, are considered.
                // If this belief is added to a specific phase, the interactions of these intents
                // with other intents of the phase have to be considered.
            } else {
                // Try to set B10

                nodes.addAll(updateB10(current, maxValue, BoundaryCheck.GEQ, constraints));
            }
        }
        // B17 can currently not be decreased

        return nodes;
    }

    /**
     * Creates a new node, following an intent edge, given the name of the intent. The node will be added to the
     * given nodes list, if it is a transition to a valid phase.
     *
     * @param nodes         The nodes list.
     * @param current       The current node.
     * @param intentName    The name of the intent for the edge.
     * @param constraints   The constraints for the current phase.
     * @return The added node.
     */
    public GraphNode updateNodeByIntentName(List<GraphNode> nodes, GraphNode current, String intentName,
                                            PhaseTransitionConstraints constraints) {
        GraphEdge edge = new GraphEdge(intentName);
        GraphNode node = getNextNode(current, edge);

        // Do not add this node if:
        // 1. The node skipped, or went a phase back
        // 2. It reached the goal phase, but without satisfying the optimal goal constraints
        if (!(node.getPhase() == constraints.getCurrentPhase() || node.getPhase() == constraints.getNextPhase())
                || (node.getPhase() == constraints.getNextPhase() && !constraintService.areDnfConstraintsSatisfied(
                        constraints.getOptimalGoal(), node.getBeliefs()))) {
            return null;
        }

        current.getEdgesFrom().add(edge);
        node.setEdgeTo(edge);
        edge.setFrom(current);
        edge.setTo(node);

        nodes.add(node);

        return node;
    }

    /**
     * Creates a new node, following an intent edge, given the name of the intent. The node will be added to the
     * given nodes list, if it is a transition to a valid phase.
     *
     * @param nodes         The nodes list.
     * @param current       The current node.
     * @param constraints   The constraints for the current phase.
     * @return The added nodes.
     */
    public List<GraphNode> updateNodeByIntentNameIntermediary(List<GraphNode> nodes, GraphNode current,
                                                              String intermediaryIntentName, String intentName,
                                                              PhaseTransitionConstraints constraints) {
        List<GraphNode> nodesInner = new ArrayList<>();
        // First node should not be added to "nodes" list, since it does not actually influence the
        // wanted belief (is an intermediary node)
        GraphNode nodeInner = updateNodeByIntentName(new ArrayList<>(), current, intermediaryIntentName, constraints);
        if (nodeInner != null) {
            nodesInner.add(nodeInner);
        }

        if (nodesInner.size() > 0) {
            nodeInner = updateNodeByIntentName(nodes, nodesInner.get(0), intentName, constraints);

            if (nodeInner != null) {
                nodesInner.add(nodeInner);
            }
        }
        return nodesInner;
    }

    /**
     * Sets the edge/perception type to "ack" and the attribute to "positive" or "negative"
     * if the current value is above or below the threshold.
     *
     * @param edge The edge
     * @param currentValue The current value of the belief
     * @param threshold The threshold for the belief
     */
    public void parseConfirmToAck(GraphEdge edge, float currentValue, float threshold) {
        edge.setType("ack");

        if (floatComparer.greaterThan(currentValue, threshold)) {
            edge.setAttribute("positive");
        } else {
            edge.setAttribute("negative");
        }
    }

    /**
     * Given the previous node, and the edge, gets the next node with updated phase, beliefs, action, desires, etc.
     *
     * @param node The previous node in the graph (i.e., LiloBots message).
     * @param edge The edge to this node in the graph (i.e., intent by user).
     */
    public GraphNode getNextNode(GraphNode node, GraphEdge edge) {
        // Initialize the next node
        GraphNode nextNode = new GraphNode(node.getBeliefs().clone());
        float[] beliefs = nextNode.getBeliefs();

        String intentionName = edge.getIntentionName();
        if (edge.getSubject().equals("unknown")) {
            edge.setSubject(node.getCurrentSubject());
            nextNode.setCurrentSubject(node.getCurrentSubject());
            intentionName = edge.getType() + "_" + edge.getSubject() + "_" + edge.getAttribute();
        } else {
            nextNode.setCurrentSubject(edge.getSubject());
        }

        // Modify beliefs according to specifics in AgentService

        if (edge.getSubject().equals("goal")) {
            graphUtils.setBeliefValue(BeliefName.B10, beliefs, maxValue);
        }

        if (edge.getSubject().equals("bullying")) {
            graphUtils.setBeliefValue(BeliefName.B10, beliefs, minValue);
        }

        switch (intentionName) {
            case "request_chitchat_greeting", "request_chitchat_faring" ->
                graphUtils.increaseBeliefValue(BeliefName.B4, beliefs, oneStep);
            case "request_chitchat_goodbye" ->
                graphUtils.setBeliefValue(BeliefName.B15, beliefs, maxValue);
            case "confirm_bullying_summary" -> {
                float hasTalkedAboutBullying = graphUtils.getBeliefValue(BeliefName.B9, beliefs);
                parseConfirmToAck(edge, hasTalkedAboutBullying, minValue);
            }
            case "ack_contactingkt_compliment", "inform_goal_help" ->
                graphUtils.increaseBeliefValue(BeliefName.B7, beliefs, oneStep);
            case "ack_bullying_empathize", "ack_goal_empathize" ->
                graphUtils.increaseBeliefValue(BeliefName.B5, beliefs, oneStep);
            case "ack_goal_compliment", "ack_confidant_compliment" ->{
                graphUtils.increaseBeliefValue(BeliefName.B1, beliefs, oneStep);
                graphUtils.increaseBeliefValue(BeliefName.B2, beliefs, oneStep); // added this
            }
            //added new case
            case "request_confidant_when", "request_confidant_feeling", "request_confidant_how", "request_confidant_say" ->
                    graphUtils.increaseBeliefValue(BeliefName.B2, beliefs, oneStep);
            case "request_goal_dream" ->
                graphUtils.setBeliefValue(BeliefName.B11, beliefs, maxValue);
            case "request_goal_feeling", "request_goal_howchild" -> {
                graphUtils.increaseBeliefValue(BeliefName.B1, beliefs, oneStep);
                graphUtils.increaseBeliefValue(BeliefName.B11, beliefs, oneStep);
            }
            case "confirm_goal_summary" -> {
                float hasTalkedAboutGoal = graphUtils.getBeliefValue(BeliefName.B10, beliefs);
                parseConfirmToAck(edge, hasTalkedAboutGoal, minValue);
            }
            case "confirm_goal_collaborate" -> {
                float hasGoodRelationWithKt = graphUtils.getBeliefValue(BeliefName.B4, beliefs);
                if (floatComparer.greaterThan(hasGoodRelationWithKt, midThreshold)) {
                    graphUtils.increaseBeliefValue(BeliefName.B7, beliefs, oneStep);
                    graphUtils.decreaseBeliefValue(BeliefName.B8, beliefs, twoSteps);
                    graphUtils.setBeliefValue(BeliefName.B16, beliefs, maxValue);
                }
                parseConfirmToAck(edge, hasGoodRelationWithKt, midThreshold);
            }
            case "inform_goal_negative" -> {
                float isCurrentlyTalkingAboutGoal = graphUtils.getBeliefValue(BeliefName.B10, beliefs);
                if (floatComparer.equalTo(isCurrentlyTalkingAboutGoal, maxValue)) {
                    graphUtils.decreaseBeliefValue(BeliefName.B7, beliefs, oneStep);
                    graphUtils.setBeliefValue(BeliefName.B8, beliefs, minValue);
                }
            }
            case "inform_goal_positive" -> {
                if (floatComparer.equalTo(graphUtils.getBeliefValue(BeliefName.B10, beliefs), maxValue)) {
                    graphUtils.setBeliefValue(BeliefName.B17, beliefs, maxValue);

                    // TODO implement action handling here, see original switch statement in AgentService
                }
            }
            case "request_confidant_who" ->
                graphUtils.setBeliefValue(BeliefName.B12, beliefs, maxValue);
            case "inform_confidant_help", "inform_confidant_say" ->
                graphUtils.setBeliefValue(BeliefName.B13, beliefs, maxValue);
            // TODO implement action handling here, see original switch statement in AgentService
            case "confirm_confidant_teacher" -> {
                float confidantCanHelp = graphUtils.getBeliefValue(BeliefName.B13, beliefs);
                if (floatComparer.greaterThan(confidantCanHelp, midThreshold)) {
                    graphUtils.setBeliefValue(BeliefName.B8, beliefs, minValue);
                    graphUtils.setBeliefValue(BeliefName.B16, beliefs, maxValue);
                }
                parseConfirmToAck(edge, confidantCanHelp, midThreshold);
            }
            case "confirm_confidant_parent" -> {
                edge.setType("ack");
                edge.setAttribute("negative");
            }
            case "confirm_confidant_summary", "confirm_chitchat_satisfaction" -> {
                edge.setType("ack");
                DesireName currentDesire = graphUtils.getActiveDesire(beliefs);
                if (currentDesire == DesireName.D4) {
                    edge.setAttribute("helpful");
                } else {
                    edge.setAttribute("negative");
                }
            }
            default -> { }
        }

        if (edge.getType().equals("request") && edge.getSubject().equals("bullying")) {
            graphUtils.increaseBeliefValue(BeliefName.B6, beliefs, oneStep);
        }

        if (edge.getType().equals("ack") && edge.getAttribute().equals("neutral")) {
            graphUtils.increaseBeliefValue(BeliefName.B5, beliefs, 0.05f);
        }

        graphUtils.setBeliefValue(BeliefName.B3, beliefs, graphUtils.calculateRelatedness(beliefs));

        // Updating of node values
        nextNode.setPhase(graphUtils.getPhaseByDesire(graphUtils.getActiveDesire(nextNode.getBeliefs())));
        nextNode.setCurrentSubject(edge.getSubject());

        return nextNode;
    }

}
