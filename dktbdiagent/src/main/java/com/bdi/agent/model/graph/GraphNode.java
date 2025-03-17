package com.bdi.agent.model.graph;

import com.bdi.agent.model.enums.Phase;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class GraphNode {
    @NotNull
    private float[] beliefs;

    @NotNull
    private List<GraphEdge> edgesFrom = new ArrayList<>();

    private GraphEdge edgeTo;

    private Phase phase;

    private String currentSubject;

    /**
     * Creates a GraphNode.
     *
     * @param beliefs The belief array.
     */
    public GraphNode(float[] beliefs) {
        this.beliefs = beliefs;
    }
}
