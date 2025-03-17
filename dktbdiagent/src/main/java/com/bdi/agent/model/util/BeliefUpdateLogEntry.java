package com.bdi.agent.model.util;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BeliefUpdateType;
import com.bdi.agent.model.enums.LogEntryType;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Entity
@NoArgsConstructor
@Data
public class BeliefUpdateLogEntry extends LogEntry {

    @Enumerated(EnumType.STRING)
    private BeliefUpdateType beliefUpdateType;

    @NotNull
    @Column(name = "new_belief_value")
    private Float value;

    @NotNull
    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private BeliefName beliefName;

    private String cause;

    @Column(name = "from_user")
    private Boolean isManualUpdate;


    /**
     * Constructor for a BeliefUpdateLogEntry that includes a timestamp. Use this when you want to log a belief update
     * with a delay. These logs are marked as non-manual updates.
     *
     * @param timestamp the timestamp
     * @param beliefUpdateType the type of update (INCREASE, DECREASE or SET_TO)
     * @param amount the resulting value, so INCREASE 0.3 means it becomes 0.3 rather than old value +0.3
     * @param beliefName the beliefName, e.g. 'B1'
     * @param cause the user's message that resulted in the belief update
     * @param agent the agent the log belongs to
     */
    public BeliefUpdateLogEntry(LocalDateTime timestamp, BeliefUpdateType beliefUpdateType,
                                Float amount, BeliefName beliefName, String cause, Agent agent) {
        super(LogEntryType.BELIEF_UPDATE, timestamp, agent);
        this.beliefUpdateType = beliefUpdateType;
        this.value = amount;
        this.beliefName = beliefName;
        this.cause = cause;
        this.isManualUpdate = false;
    }

    /**
     * Constructor for a BeliefUpdateLogEntry that does not require a timestamp. The timestamp in the log
     * will be the current time. These logs are marked as non-manual updates.
     *
     * @param beliefUpdateType the type of update (INCREASE, DECREASE or SET_TO)
     * @param amount the resulting value, so INCREASE 0.3 means it becomes 0.3 rather than old value +0.3
     * @param beliefName the beliefName, e.g. 'B1'
     * @param cause the user's message that resulted in the belief update
     * @param agent the agent the log belongs to
     */
    public BeliefUpdateLogEntry(BeliefUpdateType beliefUpdateType, Float amount, BeliefName beliefName,
                                String cause, Agent agent) {
        super(LogEntryType.BELIEF_UPDATE, agent);
        this.beliefUpdateType = beliefUpdateType;
        this.value = amount;
        this.beliefName = beliefName;
        this.cause = cause;
        this.isManualUpdate = false;
    }

    /**
     * Constructor for a BeliefUpdateLogEntry that does not require a timestamp, but allow for setting it to be a
     * manual update. The timestamp in the log will be the current time.
     *
     * @param beliefUpdateType the type of update (INCREASE, DECREASE or SET_TO)
     * @param amount the resulting value, so INCREASE 0.3 means it becomes 0.3 rather than old value +0.3
     * @param beliefName the beliefName, e.g. 'B1'
     * @param cause the user's message that resulted in the belief update
     * @param agent the agent the log belongs to
     */
    public BeliefUpdateLogEntry(BeliefUpdateType beliefUpdateType, Float amount, BeliefName beliefName,
                                String cause, Agent agent, Boolean isManualUpdate) {
        super(LogEntryType.BELIEF_UPDATE, agent);
        this.beliefUpdateType = beliefUpdateType;
        this.value = amount;
        this.beliefName = beliefName;
        this.cause = cause;
        this.isManualUpdate = isManualUpdate;
    }
}

