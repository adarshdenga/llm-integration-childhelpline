package com.bdi.agent.model.util;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.LogEntryType;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
public class DesireUpdateLogEntry extends LogEntry {

    @NotNull
    @Column(name = "new_desire_value")
    private Boolean newValue;

    @NotNull
    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private DesireName desireName;

    /**
     * Constructor for a DesireUpdateLogEntry that includes a timestamp. Use this when you want to log a desire update
     * with a delay.
     *
     * @param timestamp the timestamp
     * @param newValue the new value (Boolean) indicating whether the desire is now active or not
     * @param desireName the desire name, e.g. 'D1'
     * @param agent the agent the log belongs to
     */
    public DesireUpdateLogEntry(LocalDateTime timestamp, Boolean newValue, DesireName desireName, Agent agent) {
        super(LogEntryType.DESIRE_UPDATE, timestamp, agent);
        this.desireName = desireName;
        this.newValue = newValue;
    }

    /**
     * Constructor for a DesireUpdateLogEntry that does not require a timestamp. The timestamp in the log
     * will be the current time.
     *
     * @param newValue the new value (Boolean) indicating whether the desire is now active or not
     * @param desireName the desire name, e.g. 'D1'
     * @param agent the agent the log belongs to
     */
    public DesireUpdateLogEntry(Boolean newValue, DesireName desireName, Agent agent) {
        super(LogEntryType.DESIRE_UPDATE, agent);
        this.desireName = desireName;
        this.newValue = newValue;
    }
}
