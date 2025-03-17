package com.bdi.agent.repository;

import com.bdi.agent.model.enums.LogEntryType;
import com.bdi.agent.model.util.LogEntry;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    Set<LogEntry> findByAgentId(Long agentId);

    List<LogEntry> findByAgentIdOrderByTimestampAsc(Long agentId);

    List<LogEntry> findByAgentIdAndLogEntryType(Long agentId, LogEntryType logEntryType);

    List<LogEntry> findByAgentIdAndLogEntryTypeOrderByTimestampAsc(Long agentId, LogEntryType logEntryType);

    List<LogEntry> findByAgentIdAndLogEntryTypeAndTimestampLessThanEqualOrderByTimestampAsc(Long agent_id,
                                                                                            LogEntryType logEntryType,
                                                                                            LocalDateTime timestamp);

}
