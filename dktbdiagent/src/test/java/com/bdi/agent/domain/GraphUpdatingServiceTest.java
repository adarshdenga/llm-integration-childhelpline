package com.bdi.agent.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.bdi.agent.TestConfig;
import com.bdi.agent.TestUtils;
import com.bdi.agent.model.enums.BoundaryCheck;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.graph.GraphEdge;
import com.bdi.agent.model.graph.GraphNode;
import com.bdi.agent.model.util.BeliefConstraint;
import com.bdi.agent.model.util.PhaseTransitionConstraints;
import com.bdi.agent.service.graph.GraphUpdatingService;

@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class GraphUpdatingServiceTest {
    private transient final GraphUpdatingService graphUpdatingService;
    private transient final TestUtils utils;
    private transient final PhaseTransitionConstraints testConstraints =
            new PhaseTransitionConstraints(Phase.PHASE1, Phase.PHASE2, Map.of(), Set.of(), Set.of(), new float[]{});

    @Autowired
    public GraphUpdatingServiceTest(GraphUpdatingService graphUpdatingService, TestUtils utils) {
        this.graphUpdatingService = graphUpdatingService;
        this.utils = utils;
    }

    @ParameterizedTest
    @CsvSource({"1,0,positive", "0,1,negative", "1,1,negative"})
    public void testParseConfirmToAck(float currentValue, float threshold, String expectedAttribute) {
        GraphNode nodeA = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode nodeB = utils.createGraphNodeInPhase(Phase.PHASE2);

        GraphEdge edge = utils.createGraphEdgeConnecting(nodeA, nodeB);
        graphUpdatingService.parseConfirmToAck(edge, currentValue, threshold);
        assertThat(edge.getType()).isEqualTo("ack");
        assertThat(edge.getAttribute()).isEqualTo(expectedAttribute);
    }

    @Test
    public void testUpdateNodeByIntentNameInvalid() {
        GraphNode nodeA = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode nodeB = utils.createGraphNodeInPhase(Phase.PHASE3);

        GraphUpdatingService spyGraphUpdatingService = spy(graphUpdatingService);
        doReturn(nodeB).when(spyGraphUpdatingService).getNextNode(any(), any());

        List<GraphNode> nodes = new ArrayList<>();
        nodes.add(nodeA);

        assertThat(spyGraphUpdatingService.updateNodeByIntentName(nodes, nodeA, "type_subject_attribute",
                testConstraints)).isNull();
        assertThat(nodes).containsExactly(nodeA);
        assertThat(nodeA.getEdgesFrom()).isEmpty();
        assertThat(nodeB.getEdgesFrom()).isEmpty();
        assertThat(nodeB.getEdgeTo()).isNull();
    }

    @Test
    public void testUpdateNodeByIntentNameValid() {
        GraphNode nodeA = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode nodeB = utils.createGraphNodeInPhase(Phase.PHASE2);

        GraphUpdatingService spyGraphUpdatingService = spy(graphUpdatingService);
        doReturn(nodeB).when(spyGraphUpdatingService).getNextNode(any(), any());

        List<GraphNode> nodes = new ArrayList<>();
        nodes.add(nodeA);

        assertThat(spyGraphUpdatingService.updateNodeByIntentName(nodes, nodeA, "type_subject_attribute",
                testConstraints)).isEqualTo(nodeB);
        assertThat(nodes).containsExactlyInAnyOrder(nodeA, nodeB);

        GraphEdge edge = new GraphEdge("type_subject_attribute");
        assertThat(nodeA.getEdgesFrom()).containsExactly(edge);
        assertThat(nodeB.getEdgesFrom()).isEmpty();
        assertThat(nodeB.getEdgeTo()).isEqualTo(edge);
    }

    @Test
    public void testUpdateNodeByIntentNameIntermediaryFirstInvalid() {
        GraphNode nodeA = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode nodeB = utils.createGraphNodeInPhase(Phase.PHASE3);

        GraphUpdatingService spyGraphUpdatingService = spy(graphUpdatingService);
        doReturn(nodeB).when(spyGraphUpdatingService).getNextNode(any(), any());

        List<GraphNode> nodes = new ArrayList<>();
        nodes.add(nodeA);

        assertThat(spyGraphUpdatingService.updateNodeByIntentNameIntermediary(nodes, nodeA, "type_subject_attribute",
                "typeB_subjectB_attributeB", testConstraints)).isEmpty();
        assertThat(nodes).containsExactly(nodeA);
        assertThat(nodeA.getEdgesFrom()).isEmpty();
        assertThat(nodeB.getEdgesFrom()).isEmpty();
        assertThat(nodeB.getEdgeTo()).isNull();
    }

    @Test
    public void testUpdateNodeByIntentNameIntermediarySecondInvalid() {
        GraphNode nodeA = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode nodeB = utils.createGraphNodeInPhase(Phase.PHASE2);
        GraphNode nodeC = utils.createGraphNodeInPhase(Phase.PHASE3);

        GraphUpdatingService spyGraphUpdatingService = spy(graphUpdatingService);
        doReturn(nodeB).when(spyGraphUpdatingService).getNextNode(eq(nodeA), any());
        doReturn(nodeC).when(spyGraphUpdatingService).getNextNode(eq(nodeB), any());

        List<GraphNode> nodes = new ArrayList<>();
        nodes.add(nodeA);

        assertThat(spyGraphUpdatingService.updateNodeByIntentNameIntermediary(nodes, nodeA, "type_subject_attribute",
                "typeB_subjectB_attributeB", testConstraints)).containsExactly(nodeB);
        assertThat(nodes).containsExactlyInAnyOrder(nodeA);

        GraphEdge edge = new GraphEdge("type_subject_attribute");
        assertThat(nodeA.getEdgesFrom()).containsExactly(edge);
        assertThat(nodeB.getEdgesFrom()).isEmpty();
        assertThat(nodeB.getEdgeTo()).isEqualTo(edge);
        assertThat(nodeC.getEdgesFrom()).isEmpty();
        assertThat(nodeC.getEdgeTo()).isNull();
    }

    @Test
    public void testUpdateNodeByIntentNameIntermediaryBothValid() {
        GraphNode nodeA = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode nodeB = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode nodeC = utils.createGraphNodeInPhase(Phase.PHASE2);
        // This is so that nodeA != nodeB
        nodeB.setCurrentSubject("different subject");

        GraphUpdatingService spyGraphUpdatingService = spy(graphUpdatingService);
        doReturn(nodeB).when(spyGraphUpdatingService).getNextNode(eq(nodeA), any());
        doReturn(nodeC).when(spyGraphUpdatingService).getNextNode(eq(nodeB), any());

        List<GraphNode> nodes = new ArrayList<>();
        nodes.add(nodeA);

        assertThat(spyGraphUpdatingService.updateNodeByIntentNameIntermediary(nodes, nodeA, "type_subject_attribute",
                "typeB_subjectB_attributeB", testConstraints))
                .containsExactly(nodeB, nodeC);
        assertThat(nodes).containsExactlyInAnyOrder(nodeA, nodeC);

        GraphEdge edgeA = new GraphEdge("type_subject_attribute");
        GraphEdge edgeB = new GraphEdge("typeB_subjectB_attributeB");
        assertThat(nodeA.getEdgesFrom()).containsExactly(edgeA);
        assertThat(nodeB.getEdgesFrom()).containsExactly(edgeB);
        assertThat(nodeB.getEdgeTo()).isEqualTo(edgeA);
        assertThat(nodeC.getEdgesFrom()).isEmpty();
        assertThat(nodeC.getEdgeTo()).isEqualTo(edgeB);
    }

    @Test
    public void testUpdateNodeNotOptimalConstraint() {
        GraphNode nodeA = utils.createGraphNodeInPhase(Phase.PHASE1);
        GraphNode nodeB = utils.createGraphNodeInPhase(Phase.PHASE2);

        PhaseTransitionConstraints constraints =
                new PhaseTransitionConstraints(Phase.PHASE1, Phase.PHASE2, Map.of(), Set.of(),
                        Set.of(Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0),
                                1))), new float[]{});
        nodeB.getBeliefs()[0] = 0;

        GraphUpdatingService spyGraphUpdatingService = spy(graphUpdatingService);
        doReturn(nodeB).when(spyGraphUpdatingService).getNextNode(any(), any());

        List<GraphNode> nodes = new ArrayList<>();
        nodes.add(nodeA);

        // nodeB is in the goal phase, but does not satisfy the optimality constraint, so should not be added
        assertThat(spyGraphUpdatingService.updateNodeByIntentName(nodes, nodeA, "type_subject_attribute",
                constraints)).isNull();
        assertThat(nodes).containsExactly(nodeA);
        assertThat(nodeA.getEdgesFrom()).isEmpty();
        assertThat(nodeB.getEdgesFrom()).isEmpty();
        assertThat(nodeB.getEdgeTo()).isNull();
    }

}
