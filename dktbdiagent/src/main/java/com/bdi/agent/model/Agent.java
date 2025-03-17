package com.bdi.agent.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.util.LogEntry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table (name = "agent")
@AllArgsConstructor
@Getter
@Setter
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(unique = true)
    private String userId;  //conversation id from Rasa tracker

    private String knowledgeFile;

    @OneToMany(mappedBy="agent", cascade = CascadeType.ALL)
    private Set<Belief> beliefs;

    @OneToMany(mappedBy="agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Desire> desires;

    @Enumerated(EnumType.STRING)
    private Phase phase;

    private Long intentionId;

    // This value should not be used in the parsing of perceptions to influence the response. It is to prevent
    // NullPointerExceptions on the handling of unknown subjects.
    public String currentSubject = "DEFAULT_SUBJECT";

    @Accessors(fluent = true)
    public Boolean isActive;

    public Long currentAction;

    public float score;

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL)
    private List<LogEntry> logEntries = new ArrayList<>();

    // Flag used to indicate if the trainer is sending the responses
    @Accessors(fluent = true)
    private Boolean isTrainerResponding;

    @OneToOne(mappedBy="agent", cascade = CascadeType.ALL)
    private Conversation conversation;

    public Agent() {
        this.desires = new HashSet<>();
        this.beliefs = new HashSet<>();
        this.isTrainerResponding = false;
    }

    public Agent(String userId) {
        this.desires = new HashSet<>();
        this.beliefs = new HashSet<>();
        this.isTrainerResponding = false;
        this.userId = userId;
    }

    public void setUser(String userId) {
        this.userId = userId;
    }

}
