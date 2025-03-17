package com.bdi.agent.domain;

import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.graph.GraphEdge;
import com.bdi.agent.model.graph.GraphNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GraphEdgeTest {
    @Test
    public void testGraphEdgeValid() {
        GraphEdge edge = new GraphEdge("type_subject_attribute");

        assertThat(edge).isNotNull();
        assertThat(edge.getIntentionName()).isEqualTo("type_subject_attribute");
        assertThat(edge.getType()).isEqualTo("type");
        assertThat(edge.getSubject()).isEqualTo("subject");
        assertThat(edge.getAttribute()).isEqualTo("attribute");
        assertThat(edge.getFrom()).isNull();
        assertThat(edge.getTo()).isNull();
    }

    @Test
    public void testGraphEdgeInValid() {
        assertThatThrownBy(() -> new GraphEdge("typesubjectattribute")).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> new GraphEdge("type_subjectattribute")).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> new GraphEdge("typesubject_attribute")).isInstanceOf(Exception.class);
    }

    @Test
    public void testGraphEdgeEqualsTrue() {
        GraphEdge edgeA = new GraphEdge("type_subject_attribute");
        GraphEdge edgeB = new GraphEdge("type_subject_attribute");
        assertThat(edgeA).isEqualTo(edgeB);
        // Should be equal regardless of node "to"
        edgeB.setTo(new GraphNode(new float[]{0, 1}, new ArrayList<>(), null, Phase.PHASE1,
                "subject"));
        assertThat(edgeA).isEqualTo(edgeB);
        // Should be equal regardless of node "from"
        edgeA.setFrom(new GraphNode(new float[]{0, 1}, new ArrayList<>(), null, Phase.PHASE1,
                "subject"));
        assertThat(edgeA).isEqualTo(edgeB);
    }
}
