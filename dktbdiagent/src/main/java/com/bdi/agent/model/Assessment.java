package com.bdi.agent.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table (name = "assessment")
public class Assessment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private int prevPhase;
    private String agentId;
    private int whyleft;
    private int conversationEndMSG;
    private int condition;

    @Column(length = 5000)
    private String allBeliefs;

    private Timestamp submissionTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPrevPhase() {
        return prevPhase;
    }

    public void setPrevPhase(int prevPhase) {
        this.prevPhase = prevPhase;
    }

    public int getWhyleft() {
        return whyleft;
    }

    public void setWhyleft(int whyleft) {
        this.whyleft = whyleft;
    }

    public int getConversationEndMSG() {
        return conversationEndMSG;
    }

    public void setConversationEndMSG(int conversationEndMSG) {
        this.conversationEndMSG = conversationEndMSG;
    }

    public String getAllBeliefs() {
        return allBeliefs;
    }

    public void setAllBeliefs(String allBeliefs) {
        this.allBeliefs = allBeliefs;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Timestamp getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Timestamp submissionTime) {
        this.submissionTime = submissionTime;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }
}
