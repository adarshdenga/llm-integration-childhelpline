package com.bdi.agent.model;

import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "conversation")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Conversation {

    @Id
    @Column(name = "conversation_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long conversationId;

    @Column(name = "conversation_name")
    private String conversationName;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "report_file_path")
    private String reportFilePath;


    /**
     * Initiates a new conversation.
     *
     * @param title the title of the conversation.
     * @param timestamp the time to bind the conversation to. This is currently the generation time but can be modified
     *                  to be the end time of the session.
     * @param agent the agent that the conversation encapsulates.
     * @param user the user the conversation belongs to.
     */
    public Conversation(String title, LocalDateTime timestamp, Agent agent, User user) {
        this.conversationName = title;
        this.timestamp = timestamp;
        this.agent = agent;
        this.user = user;
        // does not initiate a report file path since the report should not exist at that point.
    }
}
