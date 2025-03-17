package com.bdi.agent.model.util;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.enums.LogEntryType;
import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "log")
@NoArgsConstructor
@Data
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade =  CascadeType.MERGE)
    @JoinColumn(name = "agent_id", referencedColumnName = "id")
    private Agent agent;

    @Enumerated(EnumType.STRING)
    private LogEntryType logEntryType;

    private LocalDateTime timestamp;

    /**
     * Constructor for a LogEntry that includes a timestamp. Use this when you want to log something with a
     * delay, such as Lilobot's messages with the delay of the typing animation length.
     *
     * @param logEntryType the type of log (e.g. MESSAGE or BELIEF_UPDATE)
     * @param timestamp the timestamp
     * @param agent the agent the log belongs to
     */
    public LogEntry(LogEntryType logEntryType, LocalDateTime timestamp, Agent agent) {
        this.logEntryType = logEntryType;
        this.timestamp = timestamp;
        this.agent = agent;
    }

    /**
     * Constructor for a LogEntry that does not require a timestamp. The timestamp in the log
     * will be the current time.
     *
     * @param logEntryType the type of log (e.g. MESSAGE or BELIEF_UPDATE)
     * @param agent the agent the log belongs to
     */
    public LogEntry(LogEntryType logEntryType, Agent agent) {
        this.logEntryType = logEntryType;
        this.timestamp = LocalDateTime.now();
        this.agent = agent;
    }

}
