package com.bdi.agent.service;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.enums.LogEntryType;
import com.bdi.agent.model.util.BeliefUpdateLogEntry;
import com.bdi.agent.model.util.LogEntry;
import com.bdi.agent.model.util.MessageLogEntry;
import com.bdi.agent.repository.LogEntryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogEntryService {

    private final LogEntryRepository logEntryRepository;

    @Autowired
    public LogEntryService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    public Set<LogEntry> getByAgent(Long agentId) {
        return logEntryRepository.findByAgentId(agentId);
    }

    public void addLogEntry(LogEntry logEntry) {
        logEntryRepository.save(logEntry);
    }

    public List<String> getLogStringsByAgent(Long agentId) {
        return logEntryRepository.findByAgentId(agentId).stream().map(LogEntry::toString).collect(Collectors.toList());
    }

    public List<LogEntry> getChronologicalLogsByAgent(Long agentId) {
        return logEntryRepository.findByAgentIdOrderByTimestampAsc(agentId);
    }

    public void addMessageLogEntry(String message, Boolean fromUser, LocalDateTime timestamp, Agent agent) {
        logEntryRepository.save(new MessageLogEntry(message, fromUser, timestamp, agent));
    }

    public void addMessageLogEntry(String message, Boolean fromUser, Agent agent) {
        logEntryRepository.save(new MessageLogEntry(message, fromUser, agent));
    }

    /**
     * Gets the list of all message logs belonging to some agent by searching for all logs of type MESSAGE
     * and then casting them to MessageLogEntry's.
     *
     * @param agentId the agent
     * @return the list of MessageLogEntry's belonging to the agent
     */
    public List<MessageLogEntry> getMessageLogsByAgent(Long agentId) {
        return logEntryRepository.findByAgentIdAndLogEntryType(agentId, LogEntryType.MESSAGE)
                .stream()
                .map(logEntry -> (MessageLogEntry) logEntry)
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of all message logs belonging to some agent in chronological order, by searching
     * for all logs of type MESSAGE sorted by timestamp and then casting them to MessageLogEntry's.
     * This may return a different order than getMessageLogsByAgent, since we allow creating logs with
     * timestamps in the future.
     *
     * @param agentId the agent
     * @return the list of MessageLogEntry's belonging to the agent, sorted by timestamp ascending
     */
    public List<MessageLogEntry> getMessageLogsByAgentChronological(Long agentId) {
        return logEntryRepository.findByAgentIdAndLogEntryTypeOrderByTimestampAsc(agentId, LogEntryType.MESSAGE)
                .stream()
                .map(logEntry -> (MessageLogEntry) logEntry)
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of all the user's (aka the learner's, not Lilo's) message logs belonging to some agent, which
     * were logged until some timestamp, sorted chronologically. This method is used when searching for candidate
     * user messages that caused some belief update. When sending belief updates to the client, the most recent match
     * is found to display in the BeliefTransition component in the front-end.
     *
     * @param agentId the agent
     * @param timestamp the latest time that will be included in the results
     * @return the list of the user's messages until the timestamp, sorted chronologically
     */
    public List<MessageLogEntry> getUserMessageLogsByAgentChronologicalUntilTimestamp(Long agentId,
                                                                                      LocalDateTime timestamp) {
        return logEntryRepository.findByAgentIdAndLogEntryTypeAndTimestampLessThanEqualOrderByTimestampAsc(agentId,
                        LogEntryType.MESSAGE, timestamp)
                .stream()
                .map(logEntry -> (MessageLogEntry) logEntry)
                .filter(MessageLogEntry::getFromUser)
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of all belief update logs belonging to some agent by searching for all logs of type BELIEF_UPDATE
     * and then casting them to BeliefUpdateLogEntry's.
     *
     * @param agentId the agent
     * @return the list of BeliefUpdateLogEntry's belonging to the agent
     */
    public List<BeliefUpdateLogEntry> getBeliefUpdateLogsByAgent(Long agentId) {
        return logEntryRepository.findByAgentIdAndLogEntryTypeOrderByTimestampAsc(agentId, LogEntryType.BELIEF_UPDATE)
                .stream()
                .map(logEntry -> (BeliefUpdateLogEntry) logEntry)
                .collect(Collectors.toList());
    }
}
