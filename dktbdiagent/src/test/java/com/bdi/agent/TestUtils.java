package com.bdi.agent;

import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.graph.GraphEdge;
import com.bdi.agent.model.graph.GraphNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@PropertySource("classpath:config.properties")
public class TestUtils {
    @Value("${minValue}")
    private float minValue;
    @Value("${maxValue}")
    private float maxValue;
    @Value("${relatednessBeliefs}")
    private BeliefName[] relatednessBeliefs;
    private final List<BeliefName> beliefOrdering = Arrays.asList(BeliefName.values());

    /**
     * Returns a float array of the size of the belief ordering filled with the given value.
     * This is to make the tests at least partially adjustable to having less/more
     * beliefs.
     *
     * @param value The value to fill the array with.
     * @return A float array of the size of the belief ordering filled with the given value.
     */
    public float[] getFilledBeliefArray(float value) {
        float[] beliefs = new float[beliefOrdering.size()];
        Arrays.fill(beliefs, value);
        return beliefs;
    }

    /**
     * Util method to create a node in a phase.
     *
     * @param phase The phase of the node.
     * @return The initialized node in the phase.
     */
    public GraphNode createGraphNodeInPhase(Phase phase) {
        return new GraphNode(new float[]{0, 1}, new ArrayList<>(), null, phase, "subject");
    }

    /**
     * Creates an edge connecting two nodes, and also adds it to the edges of those nodes.
     *
     * @param nodeA The node from which the edge goes.
     * @param nodeB The node to which the edge goes.
     * @return The edge connecting two nodes.
     */
    public GraphEdge createGraphEdgeConnecting(GraphNode nodeA, GraphNode nodeB) {
        GraphEdge edge = new GraphEdge("intention", "type", "subject", "attribute",
                nodeA, nodeB);
        nodeA.getEdgesFrom().add(edge);
        nodeB.setEdgeTo(edge);
        return edge;
    }

    /**
     * Gets the ordering of the belief names (enum values).
     *
     * @return the ordering of the belief names (enum values).
     */
    public List<BeliefName> getBeliefOrdering() {
        return beliefOrdering;
    }

    /**
     * Gets the relatedness beliefs in form of an array.
     *
     * @return the relatedness beliefs in form of an array.
     */
    public BeliefName[] getRelatednessBeliefs() {
        return relatednessBeliefs;
    }

    /**
     * Gets the min value.
     *
     * @return the min value.
     */
    public float getMinValue() {
        return minValue;
    }

    /**
     * Gets the max value.
     *
     * @return the max value.
     */
    public float getMaxValue() {
        return maxValue;
    }

}
