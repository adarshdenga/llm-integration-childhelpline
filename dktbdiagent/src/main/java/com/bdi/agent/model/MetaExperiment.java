package com.bdi.agent.model;

import javax.persistence.*;

@Entity
@Table (name = "meta_experiment")
public class MetaExperiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private int condition;
    private int knowledgetest;
    private int sessNum;
    private String knowledgeOrder;
    private String knowledgeOrderUpdt;


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

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public int getKnowledgetest() {
        return knowledgetest;
    }

    public void setKnowledgetest(int knowledge) {
        this.knowledgetest = knowledge;
    }

    public int getSessNum() {
        return sessNum;
    }

    public void setSessNum(int SessNum) {
        this.sessNum = SessNum;
    }

    public String getKnowledgeOrder() {
        return knowledgeOrder;
    }

    public void setKnowledgeOrder(String knowledgeOrder) {
        this.knowledgeOrder = knowledgeOrder;
    }

    public String getKnowledgeOrderUpdt() {
        return knowledgeOrderUpdt;
    }

    public void setKnowledgeOrderUpdt(String knowledgeOrderUpdt) {
        this.knowledgeOrderUpdt = knowledgeOrderUpdt;
    }
}
