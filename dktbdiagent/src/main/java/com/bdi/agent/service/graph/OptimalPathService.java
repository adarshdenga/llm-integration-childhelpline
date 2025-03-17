package com.bdi.agent.service.graph;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Belief;
import com.bdi.agent.model.api.OptimalPathModel;
import com.bdi.agent.model.dto.IntentDto;
import com.bdi.agent.model.dto.MessageNodeDto;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.graph.GraphEdge;
import com.bdi.agent.model.graph.GraphNode;
import com.bdi.agent.model.util.BeliefConstraint;
import com.bdi.agent.model.util.PhaseTransitionConstraints;
import com.bdi.agent.service.AgentService;
import com.bdi.agent.service.ConstraintService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OptimalPathService {
    private final AgentService agentService;
    private final GraphUpdatingService graphUpdatingService;
    private final GraphUtilsService graphUtils;
    private final ConstraintService constraintService;

    private Map<Phase, PhaseTransitionConstraints> phaseConstraintMap;
    private Map<Phase, Set<Set<BeliefConstraint>>> phaseImpossibleToReachEndMap;

    /**
     * Creates an OptimalPathService.
     *
     * @param agentService          The agentService, used to initially retrieve the agent.
     * @param graphUpdatingService  The graphUpdatingService, used to update beliefs, desires, nodes and edges.
     * @param graphUtils            The beliefDesireService, used to access/update beliefs and desires. Also
     *                              contains utility methods.
     * @param constraintService     The constraintService, used to check and handle constraints.
     */
    @Autowired
    public OptimalPathService(AgentService agentService, GraphUpdatingService graphUpdatingService,
                              GraphUtilsService graphUtils, ConstraintService constraintService) {
        this.agentService = agentService;
        this.graphUpdatingService = graphUpdatingService;
        this.graphUtils = graphUtils;
        this.constraintService = constraintService;

        this.phaseConstraintMap = constraintService.getPhaseConstraintMap();
        this.phaseImpossibleToReachEndMap = constraintService.getPhaseImpossibleToReachEndMap();
    }

    /**
     * Generates the optimal path and returns the request object for it.
     *
     * @param conversationId The id of the conversation (i.e., of the agent)
     * @return The OptimalPathRequest containing the optimal path.
     */
    public OptimalPathModel generateOptimalPathRequest(String conversationId) {
        if (!agentService.containsUserId(conversationId)) {
            throw new EntityNotFoundException("Agent not found");
        }
        Agent agent = agentService.getByUserId(conversationId);

        GraphNode startNode = initGraphFromAgent(agent);
        generateFromNode(new HashSet<>(), startNode);
        List<GraphEdge> optimalPath = findOptimalPathToPhase(startNode, Phase.PHASE5);

        if (optimalPath == null) {
            return null;
        }

        return optimalPathToPathRequest(optimalPath);
    }

    /**
     * Returns the request object for the optimal path, given the list of edges forming the path.
     *
     * @param optimalPath The list of edges forming the optimal path.
     * @return The OptimalPathRequest containing the optimal path.
     */
    public OptimalPathModel optimalPathToPathRequest(List<GraphEdge> optimalPath) {
        List<MessageNodeDto> messageNodeDtos = new ArrayList<>();
        for (int i = 0; i < optimalPath.size(); i++) {
            // Add "from" node
            GraphNode from = optimalPath.get(i).getFrom();
            IntentDto edge = new IntentDto(optimalPath.get(i).getIntentionName(),
                    graphUtils.getExampleMessage(optimalPath.get(i)));

            MessageNodeDto messageNodeDto = new MessageNodeDto(
                    graphUtils.beliefArrayToDtoList(from.getBeliefs()),
                    graphUtils.getDesiresList(from.getBeliefs()), from.getPhase(), edge);
            messageNodeDtos.add(messageNodeDto);

            // Check if it is the last edge, then also add "to" node
            if (i == optimalPath.size() - 1) {
                GraphNode to = optimalPath.get(i).getTo();
                messageNodeDto = new MessageNodeDto(
                        graphUtils.beliefArrayToDtoList(to.getBeliefs()),
                        graphUtils.getDesiresList(to.getBeliefs()), to.getPhase(), null);
                messageNodeDtos.add(messageNodeDto);
            }
        }

        return new OptimalPathModel(messageNodeDtos);
    }

    /**
     * Finds the optimal path from a start node to a certain phase. If there is none, returns null.
     *
     * @param startNode The start node to check the path from.
     * @param goalPhase The phase to arrive in.
     * @return The optimal path from a start node to a certain phase. If there is none, returns null.
     */
    public List<GraphEdge> findOptimalPathToPhase(GraphNode startNode, Phase goalPhase) {
        // Our graph is a tree, so we can perform BFS to find the first message node
        // which is in the goal phase. Then we traverse backwards until the root, and reverse
        // the collected list to get the path.

        Queue<GraphNode> traversal = new LinkedList<>();
        GraphNode current = startNode;
        traversal.add(current);

        while (!traversal.isEmpty()) {
            current = traversal.poll();

            if (current.getPhase() == goalPhase) {
                break;
            }

            Collections.shuffle(current.getEdgesFrom());

            for (GraphEdge edge : current.getEdgesFrom()) {
                traversal.add(edge.getTo());
            }
        }

        // Phase could not be reached, return null
        if (current.getPhase() != goalPhase) {
            return null;
        }

        // Otherwise, build the optimal path
        LinkedList<GraphEdge> optimalPath = new LinkedList<>();

        while (!current.equals(startNode)) {
            // Add edge to front of the optimal path list
            optimalPath.addFirst(current.getEdgeTo());

            // Get parent node
            current = current.getEdgeTo().getFrom();
        }

        return optimalPath;
    }

    /**
     * Initializes the first graph node given the agent, so generates the first node.
     *
     * @param agent The agent for which to generate the path
     * @return The initialized graph node (first node of the graph).
     */
    public GraphNode initGraphFromAgent(Agent agent) {
        float[] beliefs = new float[agent.getBeliefs().size()];
        List<Float> beliefValues = agent.getBeliefs().stream()
                .sorted(Comparator.comparing(belief -> Integer.valueOf(belief.getName().substring(1))))
                .map(Belief::getValue).toList();

        for (int i = 0; i < beliefs.length; i++) {
            beliefs[i] = beliefValues.get(i);
        }

        GraphNode startNode = new GraphNode(beliefs);
        startNode.setPhase(graphUtils.getPhaseByDesire(graphUtils.getActiveDesire(beliefs)));
        startNode.setCurrentSubject(agent.currentSubject);
        return startNode;
    }

    /**
     * Generates edges and nodes from the given node, to try to reach the last phase, if possible.
     *
     * @param added         The set of added nodes, used for testing.
     * @param node          The current node to generate from.
     * @return The set of added nodes, used for testing.
     */
    public Set<GraphNode> generateFromNode(Set<GraphNode> added, GraphNode node) {
        Phase currentPhase = node.getPhase();
        PhaseTransitionConstraints transition = phaseConstraintMap.get(node.getPhase());

        // If the node is not in starting phase, return.
        // NOTE: This scenario can only occur if the phaseConstraintMap is *malformed*. For the algorithm to function,
        // the phase the node is in, should be mapped to the PhaseTransitionConstraints from that phase.
        // Also return, if the node is in the last phase.
        if (currentPhase == Phase.PHASE5 || currentPhase != transition.getCurrentPhase()) {
            return new HashSet<>();
        }

        float[] beliefs = node.getBeliefs();

        // If the ending phase cannot be reached, return
        Set<Set<BeliefConstraint>> impossibleToReachEnd = phaseImpossibleToReachEndMap.get(currentPhase);
        if (!constraintService.isConstraintSetEmpty(impossibleToReachEnd)
                && constraintService.areDnfConstraintsSatisfied(impossibleToReachEnd, beliefs)) {
            return new HashSet<>();
        }

        // If the next phase cannot be reached, since an active desire is blocking it, return
        List<DesireName> desiresOfPhase = graphUtils.getDesiresByPhase(transition.getNextPhase());
        boolean anyDesireTrue = desiresOfPhase.stream().anyMatch(desire ->
                constraintService.checkDesireConstraints(desire, beliefs));
        if (anyDesireTrue) {
            return new HashSet<>();
        }

        // For all possible constraints which can be influenced, try to change the belief value
        // accordingly
        transition.getGoalValues().forEach((constraints, goalConstraint) -> {
            // If the condition for updating is satisfied, try to update the belief
            // to reach the goal value
            if (constraintService.areDnfConstraintsSatisfied(Set.of(constraints), beliefs)) {
                // Generate nodes to reach/get closer to goal value
                List<GraphNode> tryReachGoal = graphUpdatingService.updateBelief(
                        goalConstraint.getBeliefName(), node, goalConstraint.getGoalValue(),
                        goalConstraint.getBoundaryCheck(), transition);
                added.addAll(tryReachGoal);

                // Generate from those nodes recursively
                for (GraphNode checkNode : tryReachGoal) {
                    // If the node is in phase 5, stop generating
                    if (checkNode.getPhase() != Phase.PHASE5) {
                        generateFromNode(added, checkNode);
                    }
                }
            }
        });

        return added;
    }

    /**
     * Sets the phaseConstraintMap, used only for testing purposes.
     *
     * @param phaseConstraintMap the phaseConstraintMap.
     */
    public void setPhaseConstraintMap(Map<Phase, PhaseTransitionConstraints> phaseConstraintMap) {
        this.phaseConstraintMap = phaseConstraintMap;
    }

    /**
     * Sets the phaseImpossibleToReachEndMap, used only for testing purposes.
     *
     * @param phaseImpossibleToReachEndMap the phaseImpossibleToReachEndMap.
     */
    public void setPhaseImpossibleToReachEndMap(Map<Phase, Set<Set<BeliefConstraint>>> phaseImpossibleToReachEndMap) {
        this.phaseImpossibleToReachEndMap = phaseImpossibleToReachEndMap;
    }
}
