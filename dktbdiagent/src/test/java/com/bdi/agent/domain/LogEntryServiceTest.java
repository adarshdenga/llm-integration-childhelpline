package com.bdi.agent.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BeliefUpdateType;
import com.bdi.agent.model.enums.LogEntryType;
import com.bdi.agent.model.util.BeliefUpdateLogEntry;
import com.bdi.agent.model.util.LogEntry;
import com.bdi.agent.model.util.MessageLogEntry;
import com.bdi.agent.repository.LogEntryRepository;
import com.bdi.agent.service.LogEntryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({ "mockLogEntryRepository" })
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LogEntryServiceTest {

    @Autowired
    private transient LogEntryRepository mockLogEntryRepository;

    @Autowired
    private transient LogEntryService logEntryService;

    @Test
    public void testAddMessageLog() {
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true, 0L, 0.0f, null, false, null);
        MessageLogEntry log = new MessageLogEntry("testLog", true, agent);
        logEntryService.addMessageLogEntry("testLog", true, agent);
        ArgumentCaptor<MessageLogEntry> argumentCaptor = ArgumentCaptor.forClass(MessageLogEntry.class);
        verify(mockLogEntryRepository).save(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue(), log);
    }

    @Test
    public void testGetMessageLogsByAgentChronological() {
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true, 0L, 0.0f, null, false, null);
        MessageLogEntry log_1 = new MessageLogEntry("testLog", true, LocalDateTime.now().plusHours(1), agent);
        MessageLogEntry log_2 = new MessageLogEntry("testLog", true, agent);
        logEntryService.addLogEntry(log_1);
        logEntryService.addLogEntry(log_2);

        List<LogEntry> logs = List.of(log_2, log_1);
        List<MessageLogEntry> messageLogs = List.of(log_2, log_1);

        when(mockLogEntryRepository.findByAgentIdAndLogEntryTypeOrderByTimestampAsc(1L, LogEntryType.MESSAGE))
                .thenReturn(logs);

        List<MessageLogEntry> res = logEntryService.getMessageLogsByAgentChronological(1L);
        verify(mockLogEntryRepository).findByAgentIdAndLogEntryTypeOrderByTimestampAsc(1L, LogEntryType.MESSAGE);
        assertEquals(res, messageLogs);
    }

    @Test
    public void testGetBeliefUpdateLogsByAgent() {
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true, 0L, 0.0f, null, false, null);
        BeliefUpdateLogEntry log_1 = new BeliefUpdateLogEntry(LocalDateTime.now().minusHours(1),
                BeliefUpdateType.INCREASE,
                0.8F, BeliefName.B1, "", agent);
        logEntryService.addLogEntry(log_1);

        List<LogEntry> logs = List.of(log_1);
        List<BeliefUpdateLogEntry> belief_logs = List.of(log_1);

        when(mockLogEntryRepository.findByAgentIdAndLogEntryTypeOrderByTimestampAsc(1L, LogEntryType.BELIEF_UPDATE))
                .thenReturn(logs);

        List<BeliefUpdateLogEntry> res = logEntryService.getBeliefUpdateLogsByAgent(1L);
        verify(mockLogEntryRepository).findByAgentIdAndLogEntryTypeOrderByTimestampAsc(1L, LogEntryType.BELIEF_UPDATE);
        assertEquals(res, belief_logs);
    }
}
