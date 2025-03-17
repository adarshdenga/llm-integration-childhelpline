package com.bdi.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Perception {

    private String type;
    private String subject;
    private String attribute;
    private String text;

    public Perception(@JsonProperty("type") String type, @JsonProperty("subject") String subject, @JsonProperty("attribute") String attribute,  @JsonProperty("text") String text) {
        this.type = type;
        this.subject = subject;
        this.attribute = attribute;
        this.text = text;
    }

}
