package com.bdi.agent.model.graph;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@AllArgsConstructor
public class GraphEdge {
    @NotBlank
    private String intentionName;

    @NotBlank
    private String type;

    @NotBlank
    private String subject;

    @NotBlank
    private String attribute;

    // Edges are considered equal if they have the same name, type, subject and attribute.
    // The attributes below cannot be included, since the node refers to the edges, and otherwise there is
    // a cyclic dependency in the equals method.

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private GraphNode from;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private GraphNode to;

    /**
     * Creates a GraphEdge, given the intentionName. The other attributes can be deduced from this one.
     *
     * @param intentionName The intentionName.
     */
    public GraphEdge(String intentionName) {
        this.intentionName = intentionName;

        String[] splitName = intentionName.split("_");
        this.type = splitName[0];
        this.subject = splitName[1];
        this.attribute = splitName[2];
    }
}
