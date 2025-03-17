package com.bdi.agent.model.util;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.LogEntryType;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import reactor.util.annotation.Nullable;

@Entity
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class MessageLogEntry extends LogEntry {

    private String message;

    @Column(name = "from_user")
    private Boolean fromUser;

    @Nullable
    @Enumerated(EnumType.STRING)
    private DesireName intention; // intention is first active desire

    /**
     * Constructor for a MessageLogEntry that includes a timestamp and an intention. Use this when you want to log
     * a message from Lilobot with a delay.
     *
     * @param timestamp the timestamp
     * @param message the message
     * @param fromUser whether the user sent the message (so false if Lilo sent it)
     * @param agent the agent the log belongs to
     * @param intention the intention that Lilobot had when sending the message
     */
    public MessageLogEntry(String message, Boolean fromUser, LocalDateTime timestamp,
                           Agent agent, DesireName intention) {
        super(LogEntryType.MESSAGE, timestamp, agent);
        this.message = message;
        this.fromUser = fromUser;
        this.intention = intention;
    }

    /**
     * Constructor for a MessageLogEntry that includes a timestamp. Use this when you want to log a message with
     * a delay. This constructor is recommended for messages that are from the learner, since it does not require
     * an intention to be bound to the message
     *
     * @param timestamp the timestamp
     * @param message the message
     * @param fromUser whether the user sent the message (so false if Lilo sent it)
     * @param agent the agent the log belongs to
     */
    public MessageLogEntry(String message, Boolean fromUser, LocalDateTime timestamp,
                           Agent agent) {
        super(LogEntryType.MESSAGE, timestamp, agent);
        this.message = message;
        this.fromUser = fromUser;
    }

    /**
     * Constructor for a MessageLogEntry that does not require a timestamp, but requires an intention. The timestamp
     * in the log will be the current time. Use this when you want to log a message from Lilobot.
     *
     * @param message the message
     * @param fromUser whether the user sent the message (so false if Lilo sent it)
     * @param agent the agent the log belongs to
     * @param intention the intention that Lilobot had when sending the message
     */
    public MessageLogEntry(String message, Boolean fromUser, Agent agent, DesireName intention) {
        super(LogEntryType.MESSAGE, agent);
        this.message = message;
        this.fromUser = fromUser;
        this.intention = intention;
    }

    /**
     * Constructor for a MessageLogEntry that does not require a timestamp, but requires an intention. The timestamp
     * in the log will be the current time. Use this when you want to log a message from the learner.
     *
     * @param message the message
     * @param fromUser whether the user sent the message (so false if Lilo sent it)
     * @param agent the agent the log belongs to
     */
    public MessageLogEntry(String message, Boolean fromUser, Agent agent) {
        super(LogEntryType.MESSAGE, agent);
        this.message = message;
        this.fromUser = fromUser;
    }
}
