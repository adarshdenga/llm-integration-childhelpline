package com.bdi.agent.domain;

import com.bdi.agent.TestConfig;
import com.bdi.agent.TestUtils;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Belief;
import com.bdi.agent.model.Desire;
import com.bdi.agent.model.api.OptimalPathModel;
import com.bdi.agent.model.dto.BeliefDto;
import com.bdi.agent.model.dto.DesireDto;
import com.bdi.agent.model.dto.IntentDto;
import com.bdi.agent.model.dto.MessageNodeDto;
import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BoundaryCheck;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.graph.GraphEdge;
import com.bdi.agent.model.graph.GraphNode;
import com.bdi.agent.model.util.BeliefConstraint;
import com.bdi.agent.model.util.PhaseTransitionConstraints;
import com.bdi.agent.service.AgentService;
import com.bdi.agent.service.ConstraintService;
import com.bdi.agent.service.graph.GraphUpdatingService;
import com.bdi.agent.service.graph.GraphUtilsService;
import com.bdi.agent.service.graph.OptimalPathService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles({"mockConstraintService", "mockConstraintProvider", "mockGraphUpdatingService", "mockAgentService"})
@TestPropertySource(locations="classpath:application-test.properties")
public class OptimalPathServiceTest {
    private transient final ConstraintService mockConstraintService;
    private transient final GraphUpdatingService mockGraphUpdatingService;
    private transient final AgentService mockAgentService;
    private transient final OptimalPathService optimalPathService;
    private transient final GraphUtilsService graphUtils;
    private transient final TestUtils utils;

    @Autowired
    public OptimalPathServiceTest(ConstraintService mockConstraintService,
                                  GraphUpdatingService mockGraphUpdatingService, AgentService mockAgentService,
                                  OptimalPathService optimalPathService, GraphUtilsService graphUtils,
                                  TestUtils utils) {
        this.mockConstraintService = mockConstraintService;
        this.mockGraphUpdatingService = mockGraphUpdatingService;
        this.mockAgentService = mockAgentService;
        this.optimalPathService = optimalPathService;
        this.graphUtils = graphUtils;
        this.utils = utils;
    }

    @Test
    public void testGenerateOptimalPathRequestError() {
        when(mockAgentService.containsUserId(any())).thenReturn(false);

        assertThatThrownBy(() ->
                optimalPathService.generateOptimalPathRequest("testId"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Agent not found");
    }

    @Test
    public void testGenerateOptimalPathRequestNull() {
        /*
        Note: This test is very closely coupled to the current implementation due to the complexity of
        findOverallOptimalPath and not being able to assert on the result of the method.
         */

        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true,
                0L, 0.0f, null, false, null);
        when(mockAgentService.containsUserId("testId")).thenReturn(true);
        when(mockAgentService.getByUserId("testId")).thenReturn(agent);

        OptimalPathService spyPathService = spy(optimalPathService);
        doReturn(null).when(spyPathService).initGraphFromAgent(any());
        doReturn(new HashSet<>()).when(spyPathService).generateFromNode(any(), any());
        doReturn(null).when(spyPathService).findOptimalPathToPhase(any(), any());

        // Check that it tries to get to the correct phase
        assertThat(spyPathService.generateOptimalPathRequest("testId")).isNull();
        verify(spyPathService).findOptimalPathToPhase(null, Phase.PHASE5);
    }

    @Test
    public void testGenerateOptimalPathRequestNonNull() {
        /*
        Note: This test is very closely coupled to the current implementation due to the complexity of
        findOverallOptimalPath and not being able to assert on the result of the method.
         */

        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true,
                0L, 0.0f, null, false, null);
        when(mockAgentService.containsUserId("testId")).thenReturn(true);
        when(mockAgentService.getByUserId("testId")).thenReturn(agent);

        // Return an empty list. Other examples are tested in tests for optimalPathToPathRequest
        OptimalPathService spyPathService = spy(optimalPathService);
        doReturn(null).when(spyPathService).initGraphFromAgent(any());
        doReturn(new HashSet<>()).when(spyPathService).generateFromNode(any(), any());
        doReturn(new ArrayList<>()).when(spyPathService).findOptimalPathToPhase(any(), any());

        // Check that it tries to get to the correct phase
        assertThat(spyPathService.generateOptimalPathRequest("testId"))
                .isEqualTo(new OptimalPathModel(new ArrayList<>()));
        verify(spyPathService).findOptimalPathToPhase(null, Phase.PHASE5);
    }

    @Test
    public void testOptimalPathToPathRequestEmpty() {
        assertThat(optimalPathService.optimalPathToPathRequest(new ArrayList<>()))
                .isEqualTo(new OptimalPathModel(new ArrayList<>()));
    }

    @Test
    public void testOptimalPathToPathRequest() {
        // --- Create a path with three nodes ---
        GraphNode firstNode = new GraphNode(new float[]{0, 1}, new ArrayList<>(), null, Phase.PHASE1,
                "subject");
        GraphNode secondNode = new GraphNode(new float[]{0, 1}, new ArrayList<>(), null, Phase.PHASE2,
                "subject");
        GraphNode thirdNode = new GraphNode(new float[]{1, 1}, new ArrayList<>(), null, Phase.PHASE2,
                "subject");

        GraphEdge firstEdge = new GraphEdge("intention1", "type", "subject", "attribute",
                firstNode, secondNode);
        GraphEdge secondEdge = new GraphEdge("intention2", "type", "subject", "attribute",
                secondNode, thirdNode);

        firstNode.getEdgesFrom().add(firstEdge);
        secondNode.getEdgesFrom().add(secondEdge);
        secondNode.setEdgeTo(firstEdge);
        thirdNode.setEdgeTo(secondEdge);

        List<GraphEdge> path = List.of(firstEdge, secondEdge);

        // --- Create a MessageNodeDto of the path ---
        List<BeliefDto> beliefsA = new ArrayList<>();
        beliefsA.add(new BeliefDto("B1", "belief", 0f));
        beliefsA.add(new BeliefDto("B2", "belief", 1f));
        List<BeliefDto> beliefsB = new ArrayList<>();
        beliefsB.add(new BeliefDto("B1", "belief", 1f));
        beliefsB.add(new BeliefDto("B2", "belief", 1f));

        List<DesireDto> desires = new ArrayList<>();
        desires.add(new DesireDto(DesireName.D1, "desire", true));

        IntentDto firstIntent = new IntentDto("intention1", "msg");
        MessageNodeDto firstMessageNode = new MessageNodeDto(beliefsA, desires, Phase.PHASE1, firstIntent);
        IntentDto secondIntent = new IntentDto("intention2", "msg");
        MessageNodeDto secondMessageNode = new MessageNodeDto(beliefsA, desires, Phase.PHASE2, secondIntent);
        MessageNodeDto thirdMessageNode = new MessageNodeDto(beliefsB, desires, Phase.PHASE2, null);

        List<MessageNodeDto> messageNodes = List.of(firstMessageNode, secondMessageNode, thirdMessageNode);

        // --- Mock example message ---
        GraphUtilsService spyGraphUtils = spy(graphUtils);
        doReturn("msg").when(spyGraphUtils).getExampleMessage(any());
        doReturn("belief").when(spyGraphUtils).getFullBeliefName(any());
        doReturn(desires).when(spyGraphUtils).getDesiresList(any());

        OptimalPathService innerOptimalPathService = new OptimalPathService(mockAgentService, mockGraphUpdatingService,
                spyGraphUtils, mockConstraintService);

        assertThat(innerOptimalPathService.optimalPathToPathRequest(path)).isEqualTo(new OptimalPathModel(messageNodes));
    }

    @Test
    public void testFindOptimalPathToPhaseExists() {
        /* Create a tree with eight nodes as follows:
                 1
               / | \
              1  1  2  <-- this is the optimal paths end
              |  |
              1  2
             / \
             2  1  <-- an example of a dead end
         */

        // Nodes/edges are numbered left to right, up to down
        GraphNode node1 = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode node2 = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode node3 = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode node4 = utils.createGraphNodeInPhase(Phase.PHASE2);
        GraphNode node5 = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode node6 = utils.createGraphNodeInPhase(Phase.PHASE2);
        GraphNode node7 = utils.createGraphNodeInPhase(Phase.PHASE2);
        GraphNode node8 = utils.createGraphNodeInPhase(Phase.PHASE1);

        utils.createGraphEdgeConnecting(node1, node2);
        utils.createGraphEdgeConnecting(node1, node3);
        GraphEdge edge3 = utils.createGraphEdgeConnecting(node1, node4);
        utils.createGraphEdgeConnecting(node2, node5);
        utils.createGraphEdgeConnecting(node3, node6);
        utils.createGraphEdgeConnecting(node5, node7);
        utils.createGraphEdgeConnecting(node5, node8);

        assertThat(optimalPathService.findOptimalPathToPhase(node1, Phase.PHASE2))
                .containsExactly(edge3);
    }

    @Test
    public void testFindOptimalPathToPhaseDoesNotExist() {
        /* Create a tree with four nodes as follows:
                1
               /  \
              1    1
              |
              1
         */

        // Nodes/edges are numbered left to right, up to down
        GraphNode node1 = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode node2 = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode node3 = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode node4 = utils.createGraphNodeInPhase(Phase.PHASE1);

        utils.createGraphEdgeConnecting(node1, node2);
        utils.createGraphEdgeConnecting(node1, node3);
        utils.createGraphEdgeConnecting(node2, node4);

        assertThat(optimalPathService.findOptimalPathToPhase(node1, Phase.PHASE2)).isNull();
    }

    @Test
    public void testFindOptimalPathToPhaseAlreadyInPhase() {
        /* Create a tree with three nodes as follows:
                2
               /  \
              2    2
         */

        // Nodes/edges are numbered left to right, up to down
        GraphNode node1 = utils.createGraphNodeInPhase(Phase.PHASE2);
        GraphNode node2 = utils.createGraphNodeInPhase(Phase.PHASE2);
        GraphNode node3 = utils.createGraphNodeInPhase(Phase.PHASE2);

        utils.createGraphEdgeConnecting(node1, node2);
        utils.createGraphEdgeConnecting(node1, node3);

        assertThat(optimalPathService.findOptimalPathToPhase(node1, Phase.PHASE2)).isEmpty();
    }

    @Test
    public void testInitGraphFromAgent() {
        Set<Belief> beliefs = new HashSet<>();
        beliefs.add(new Belief("B1", "full belief", 0.5f));
        beliefs.add(new Belief("B2", "full belief", 1f));

        Set<Desire> desires = new HashSet<>();
        Agent agent = new Agent(1L, "testId", "test", beliefs, desires, null, 0L, "subject", true,
                0L, 0.0f, null, false, null);

        GraphUtilsService spyGraphUtils = spy(graphUtils);
        doReturn(DesireName.D3).when(spyGraphUtils).getActiveDesire(any());
        doReturn(Phase.PHASE2).when(spyGraphUtils).getPhaseByDesire(any());

        OptimalPathService innerOptimalPathService = new OptimalPathService(mockAgentService, mockGraphUpdatingService,
                spyGraphUtils, mockConstraintService);

        GraphNode expected = new GraphNode(new float[]{0.5f, 1f}, new ArrayList<>(), null, Phase.PHASE2,
                "subject");

        assertThat(innerOptimalPathService.initGraphFromAgent(agent)).isEqualTo(expected);
    }

    @Test
    public void testGenerateNodeNotInCorrectPhase() {
        // NOTE: This scenario can only occur if the phaseConstraintMap is *malformed*. For the algorithm to function,
        // the phase the node is in, should be mapped to the PhaseTransitionConstraints from that phase.
        GraphNode node = utils.createGraphNodeInPhase(Phase.PHASE1);
        PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                Phase.PHASE2, Phase.PHASE3, Map.of(), Set.of(), Set.of(), new float[]{});
        optimalPathService.setPhaseConstraintMap(Map.of(Phase.PHASE1, constraints));

        assertThat(optimalPathService.generateFromNode(new HashSet<>(), node)).isEmpty();

        // Should not generate from phase 5
        assertThat(optimalPathService.generateFromNode(new HashSet<>(), utils.createGraphNodeInPhase(Phase.PHASE5)))
                .isEmpty();
    }

    @Test
    public void testGenerateNodeImpossibleToReach() {
        GraphNode node = utils.createGraphNodeInPhase(Phase.PHASE1);
        BeliefConstraint constraint = new BeliefConstraint(BoundaryCheck.LT, BeliefName.B1, 1f);
        PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                Phase.PHASE1, Phase.PHASE2, Map.of(), Set.of(Set.of(constraint)), Set.of(), new float[]{});
        optimalPathService.setPhaseConstraintMap(Map.of(Phase.PHASE1, constraints));
        optimalPathService.setPhaseImpossibleToReachEndMap(Map.of(Phase.PHASE1, Set.of(Set.of(constraint))));

        when(mockConstraintService.isConstraintSetEmpty(any())).thenReturn(false);
        when(mockConstraintService.areDnfConstraintsSatisfied(Set.of(Set.of(constraint)), node.getBeliefs()))
                .thenReturn(true);

        assertThat(optimalPathService.generateFromNode(new HashSet<>(), node)).isEmpty();
    }

    @Test
    public void testGenerateNodeImpossibleToReachTransitionDifferentConstraints() {
        GraphNode node = utils.createGraphNodeInPhase(Phase.PHASE1);
        BeliefConstraint constraint = new BeliefConstraint(BoundaryCheck.LT, BeliefName.B1, 1f);
        PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                Phase.PHASE1, Phase.PHASE2, Map.of(), Set.of(Set.of(constraint)), Set.of(), new float[]{});
        optimalPathService.setPhaseConstraintMap(Map.of(Phase.PHASE1, constraints));
        optimalPathService.setPhaseImpossibleToReachEndMap(Map.of(Phase.PHASE1, Set.of()));

        when(mockConstraintService.isConstraintSetEmpty(any())).thenReturn(false);
        when(mockConstraintService.areDnfConstraintsSatisfied(Set.of(), node.getBeliefs()))
                .thenReturn(true);

        assertThat(optimalPathService.generateFromNode(new HashSet<>(), node)).isEmpty();
    }

    @Test
    public void testGenerateNodeGoalValuesEmpty() {
        GraphNode node = utils.createGraphNodeInPhase(Phase.PHASE1);
        BeliefConstraint constraint = new BeliefConstraint(BoundaryCheck.LT, BeliefName.B1, 1f);
        PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                Phase.PHASE1, Phase.PHASE2, Map.of(), Set.of(Set.of(constraint)), Set.of(), new float[]{});
        optimalPathService.setPhaseConstraintMap(Map.of(Phase.PHASE1, constraints));
        optimalPathService.setPhaseImpossibleToReachEndMap(Map.of(Phase.PHASE1, Set.of()));

        when(mockConstraintService.checkDesireConstraints(any(), any())).thenReturn(false);
        when(mockConstraintService.areCnfConstraintsSatisfied(any(), any())).thenReturn(false);
        when(mockConstraintService.areDnfConstraintsSatisfied(any(), any())).thenReturn(false);

        assertThat(optimalPathService.generateFromNode(new HashSet<>(), node)).isEmpty();
    }

    @Test
    public void testGenerateNodePreviousDesireBlocks() {
        GraphNode node = utils.createGraphNodeInPhase(Phase.PHASE1);
        BeliefConstraint constraint = new BeliefConstraint(BoundaryCheck.LT, BeliefName.B1, 1f);
        PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                Phase.PHASE1, Phase.PHASE2, Map.of(Set.of(constraint), constraint), Set.of(), Set.of(), new float[]{});
        optimalPathService.setPhaseConstraintMap(Map.of(Phase.PHASE1, constraints));
        optimalPathService.setPhaseImpossibleToReachEndMap(Map.of(Phase.PHASE1, Set.of()));

        when(mockConstraintService.isConstraintSetEmpty(any())).thenReturn(true);
        when(mockConstraintService.checkDesireConstraints(any(), any())).thenReturn(true);
        when(mockConstraintService.areDnfConstraintsSatisfied(any(), any())).thenReturn(true);

        assertThat(optimalPathService.generateFromNode(new HashSet<>(), node)).isEmpty();
    }

    @Test
    public void testGenerateNodeGoalValuesAllNotSatisfied() {
        GraphNode node = utils.createGraphNodeInPhase(Phase.PHASE1);
        BeliefConstraint constraint = new BeliefConstraint(BoundaryCheck.LT, BeliefName.B1, 1f);
        PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                Phase.PHASE1, Phase.PHASE2, Map.of(Set.of(constraint), constraint), Set.of(), Set.of(), new float[]{});
        optimalPathService.setPhaseConstraintMap(Map.of(Phase.PHASE1, constraints));
        optimalPathService.setPhaseImpossibleToReachEndMap(Map.of(Phase.PHASE1, Set.of()));

        when(mockConstraintService.isConstraintSetEmpty(any())).thenReturn(true);
        when(mockConstraintService.checkDesireConstraints(any(), any())).thenReturn(false);
        when(mockConstraintService.areDnfConstraintsSatisfied(any(), any())).thenReturn(false);

        assertThat(optimalPathService.generateFromNode(new HashSet<>(), node)).isEmpty();
    }


    @Test
    public void testGenerateNodeAddingOneNode() {
        GraphNode node = utils.createGraphNodeInPhase(Phase.PHASE4);
        BeliefConstraint constraint = new BeliefConstraint(BoundaryCheck.LT, BeliefName.B1, 1f);
        PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                Phase.PHASE4, Phase.PHASE5, Map.of(Set.of(constraint), constraint), Set.of(), Set.of(), new float[]{});
        optimalPathService.setPhaseConstraintMap(Map.of(Phase.PHASE4, constraints));
        optimalPathService.setPhaseImpossibleToReachEndMap(Map.of(Phase.PHASE4, Set.of()));

        when(mockConstraintService.isConstraintSetEmpty(any())).thenReturn(true);
        when(mockConstraintService.checkDesireConstraints(any(), any())).thenReturn(false);
        when(mockConstraintService.areDnfConstraintsSatisfied(any(), any())).thenReturn(true);

        GraphNode added = utils.createGraphNodeInPhase(Phase.PHASE5);
        doReturn(List.of(added)).when(mockGraphUpdatingService).updateBelief(any(), any(), anyFloat(), any(), any());

        assertThat(optimalPathService.generateFromNode(new HashSet<>(), node)).containsExactly(added);
    }

    @Test
    public void testGenerateNodeAddingDifferentAmountOfNodes() {
        GraphNode node = utils.createGraphNodeInPhase(Phase.PHASE4);
        BeliefConstraint constraint = new BeliefConstraint(BoundaryCheck.LT, BeliefName.B1, 1f);
        PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                Phase.PHASE4, Phase.PHASE5, Map.of(Set.of(constraint), constraint), Set.of(), Set.of(), new float[]{});
        optimalPathService.setPhaseConstraintMap(Map.of(Phase.PHASE4, constraints));
        optimalPathService.setPhaseImpossibleToReachEndMap(Map.of(Phase.PHASE4, Set.of()));

        when(mockConstraintService.isConstraintSetEmpty(any())).thenReturn(true);
        when(mockConstraintService.checkDesireConstraints(any(), any())).thenReturn(false);
        when(mockConstraintService.areDnfConstraintsSatisfied(any(), any())).thenReturn(true);

        GraphNode added1 = utils.createGraphNodeInPhase(Phase.PHASE4);
        // This is so that added1 != node
        added1.setCurrentSubject("different subject");
        GraphNode added2 = utils.createGraphNodeInPhase(Phase.PHASE5);
        doReturn(List.of(added1)).when(mockGraphUpdatingService).updateBelief(any(), eq(node), anyFloat(),
                any(), any());
        doReturn(List.of(added2)).when(mockGraphUpdatingService).updateBelief(any(), eq(added1), anyFloat(),
                any(), any());

        assertThat(optimalPathService.generateFromNode(new HashSet<>(), node))
                .containsExactlyInAnyOrder(added1, added2);
        assertThat(optimalPathService.generateFromNode(new HashSet<>(), added1)).containsExactlyInAnyOrder(added2);
        assertThat(optimalPathService.generateFromNode(new HashSet<>(), added2)).isEmpty();
    }

    @Test
    public void testGenerateNodeOverMultiplePhases() {
        GraphNode node = utils.createGraphNodeInPhase(Phase.PHASE3);
        BeliefConstraint constraint = new BeliefConstraint(BoundaryCheck.LT, BeliefName.B1, 1f);
        PhaseTransitionConstraints constraints34 = new PhaseTransitionConstraints(
                Phase.PHASE3, Phase.PHASE4, Map.of(Set.of(constraint), constraint), Set.of(), Set.of(), new float[]{});
        PhaseTransitionConstraints constraints45 = new PhaseTransitionConstraints(
                Phase.PHASE4, Phase.PHASE5, Map.of(Set.of(constraint), constraint), Set.of(), Set.of(), new float[]{});
        optimalPathService.setPhaseConstraintMap(Map.of(Phase.PHASE3, constraints34, Phase.PHASE4, constraints45));
        optimalPathService.setPhaseImpossibleToReachEndMap(Map.of(Phase.PHASE3, Set.of(), Phase.PHASE4, Set.of()));

        when(mockConstraintService.isConstraintSetEmpty(any())).thenReturn(true);
        when(mockConstraintService.checkDesireConstraints(any(), any())).thenReturn(false);
        when(mockConstraintService.areDnfConstraintsSatisfied(any(), any())).thenReturn(true);

        GraphNode added1 = utils.createGraphNodeInPhase(Phase.PHASE4);
        // This is so that added1 != node
        added1.setCurrentSubject("different subject");
        GraphNode added2 = utils.createGraphNodeInPhase(Phase.PHASE5);
        doReturn(List.of(added1)).when(mockGraphUpdatingService).updateBelief(any(), eq(node), anyFloat(),
                any(), any());
        doReturn(List.of(added2)).when(mockGraphUpdatingService).updateBelief(any(), eq(added1), anyFloat(),
                any(), any());

        assertThat(optimalPathService.generateFromNode(new HashSet<>(), node))
                .containsExactlyInAnyOrder(added1, added2);
        assertThat(optimalPathService.generateFromNode(new HashSet<>(), added1)).containsExactlyInAnyOrder(added2);
        assertThat(optimalPathService.generateFromNode(new HashSet<>(), added2)).isEmpty();
    }
}
