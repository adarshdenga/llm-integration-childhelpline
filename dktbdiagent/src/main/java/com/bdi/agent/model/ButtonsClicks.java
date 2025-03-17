package com.bdi.agent.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "buttons_clicks")
public class ButtonsClicks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clickid;

    private String username;
    private int buttonClicked;
    private Timestamp currentTime;
    private int phase;
    private boolean firstSession;
    private String currentBeliefs;
    private int condition;


    private String agentId;

    // Getters and Setters

    public Long getClickid() {
        return clickid;
    }
    public boolean isFirstSession() {
        return firstSession;
    }

    public String getUsername() {
        return username;
    }

    public int getButtonClicked() {
        return buttonClicked;
    }

    public Timestamp getCurrentTime() {
        return currentTime;
    }

    public int getPhase() {
        return phase;
    }

    public String getCurrentBeliefs() {
        return currentBeliefs;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setClickid(Long clickid) {
        this.clickid = clickid;
    }
    public void setFirstSession(boolean firstSession) {
        this.firstSession = firstSession;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setButtonClicked(int buttonClicked) {
        this.buttonClicked = buttonClicked;
    }

    public void setCurrentTime(Timestamp currentTime) {
        this.currentTime = currentTime;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public void setCurrentBeliefs(String currentBeliefs) {
        this.currentBeliefs = currentBeliefs;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }
}