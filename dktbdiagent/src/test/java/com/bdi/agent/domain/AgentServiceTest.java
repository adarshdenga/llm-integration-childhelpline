package com.bdi.agent.domain;

import com.bdi.agent.exceptions.SizeMismatchException;
import com.bdi.agent.model.*;
import com.bdi.agent.model.api.BeliefChangeModel;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.util.DesireUpdateLogEntry;
import com.bdi.agent.model.util.MessageLogEntry;
import com.bdi.agent.model.util.PhaseTransitionConstraints;
import com.bdi.agent.repository.AgentRepository;
import com.bdi.agent.repository.BeliefRepository;
import com.bdi.agent.service.ActionService;
import com.bdi.agent.service.AgentService;
import com.bdi.agent.service.BeliefService;
import com.bdi.agent.service.ConstraintService;
import com.bdi.agent.service.DesireService;
import com.bdi.agent.service.KnowledgeService;
import com.bdi.agent.service.LogEntryService;
import java.util.List;

import com.bdi.agent.utils.ConstraintProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"mockBeliefRepository", "mockAgentRepository", "mockBeliefService",
        "mockDesireService", "mockActionService", "mockKnowledgeService", "mockConstraintService",
        "mockLogEntryService", "mockLogEntryRepository", "mockConstraintProvider", "mockSimpMessagingTemplate"})
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AgentServiceTest {
    float minValue = (float) 0;
    float maxValue = (float) 1;

    @Autowired
    private transient BeliefRepository mockBeliefRepository;

    @Autowired
    private transient ConstraintService mockConstraintService;

    @Autowired
    private transient AgentRepository mockAgentRepository;

    @Autowired
    private transient AgentService agentService;

    @Autowired
    private transient LogEntryService logEntryService;

    @Autowired
    private transient BeliefService beliefService;

    @Autowired
    private transient DesireService desireService;

    @Autowired
    private transient ActionService actionService;

    @Autowired
    private transient KnowledgeService knowledgeService;

    @Autowired
    private transient ConstraintProvider constraintProvider;

    @Autowired
    private transient SimpMessagingTemplate messagingTemplate;

    @Test
    public void updateBeliefInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            agentService.updateBelief("testId", "B1", minValue - 0.1f);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            agentService.updateBelief("testId", "B1", maxValue + 0.1f);
        });
    }

    @Test
    public void updateBeliefInvalidConversationId() {
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> {
            agentService.updateBelief("testId", "B1", minValue);
        });
    }


    @Test
    public void updateBeliefValid() {
        Agent agent = new Agent(1L, "testId", "testKnowledge", null, null, null, 0L, "", true, 0L, 0.0f, null, false, null);
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
        when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
        agentService.updateBelief("testId", "B1", maxValue);
        verify(beliefService).setBeliefValue(agent, "B1", maxValue);
    }

    @Test
    public void testCheckDesireConstraints() {
        // This test only asserts that the correct methods are called, and that the corresponding
        // value is returned (it should simply return the result of checkDesireConstraints)

        float[] values = new float[]{0, 0, 0};
        when(beliefService.getValuesByAgentSortByName(1L)).thenReturn(values);
        when(mockConstraintService.checkDesireConstraints(DesireName.D1, values)).thenReturn(true);

        agentService.checkDesireConstraints(1L, DesireName.D1);

        verify(beliefService).getValuesByAgentSortByName(1L);
        verify(mockConstraintService).checkDesireConstraints(DesireName.D1, values);
    }

    @Test
    public void setManualInvalid() {
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> {
            agentService.setTrainerResponding("testId", false);
        });
    }

    @Test
    public void setManualValid() {
        Agent agent = new Agent(1L, "testId", "testKnowledge", null, null, null, 0L, "", true, 0L, 0.0f, null, false, null);
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
        when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
        agentService.setTrainerResponding("testId", true);
        ArgumentCaptor<Agent> argumentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(mockAgentRepository).save(argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue().isTrainerResponding());
    }

    @Test
    public void setManualValidFalse() {
        Agent agent = new Agent(1L, "testId", "knowledge", null, null, null, 0L, "", true, 0L, 0.0f, null, true, null);
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
        when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
        agentService.setTrainerResponding("testId", false);
        ArgumentCaptor<Agent> argumentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(mockAgentRepository).save(argumentCaptor.capture());
        assertFalse(argumentCaptor.getValue().isTrainerResponding());
    }


    @Test
    public void addLogValidLilo() {
        // The next 2 tests only asserts that the correct methods are called, with the correct arguments. The tests
        // for saving the logs to the repository from the logEntryService can be found in LogEntryServiceTest

        Agent agent = new Agent(1L, "testId", "knowledge", null, null, null, 0L, "", true, 0L, 0.0f, new ArrayList<>(), true, null);
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
        when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
        agentService.addLog(new MessageLogEntry("testLog", false, agent));
        ArgumentCaptor<MessageLogEntry> argumentCaptor = ArgumentCaptor.forClass(MessageLogEntry.class);
        verify(logEntryService).addLogEntry(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue(), new MessageLogEntry("testLog", false, agent));
    }


    @Test
    public void addLogValidKt() {
        Agent agent = new Agent(1L, "testId", "knowledge", null, null, null, 0L, "", true, 0L, 0.0f, new ArrayList<>(), true, null);
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
        when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
        agentService.addLog(new MessageLogEntry("testLog", true, agent));
        ArgumentCaptor<MessageLogEntry> argumentCaptor = ArgumentCaptor.forClass(MessageLogEntry.class);
        verify(logEntryService).addLogEntry(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue(), new MessageLogEntry("testLog", true, agent));
    }

    @Test
    public void reasonTest() {
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true, 0L, 0.0f, new ArrayList<>(), false, null);
        Desire desire = new Desire(0L, agent, "", "", true, null);
        when(desireService.getById(0L)).thenReturn(desire);
        Action action = new Action(desire, "", "", "s", "a", false);
        when(actionService.getUncompletedAction(0L)).thenReturn(action);
        Knowledge knowledge = new Knowledge("test", "s", "a");
        when(knowledgeService.getBySubjectAndAttribute("test", "s", "a")).thenReturn(knowledge);
        when(knowledgeService.getResponse(knowledge)).thenReturn("response");

        String response = agentService.reason(agent, new Perception("trigger", "s", "a", ""));

        assertEquals(response, "response");
    }

    @Test
    public void addDesireUpdateLogTest() {
        Agent agent = new Agent(1L, "testId", "testKnowledge", null, null, null, 0L, "", true, 0L, 0.0f, new ArrayList<>(), false, null);
        Desire desire = new Desire(0L, agent, "D1", "", true, null);
        when(desireService.getById(0L)).thenReturn(desire);
        when(desireService.getByAgent(1L)).thenReturn(List.of(desire));
        Action action = new Action(desire, "", "", "s", "a", false);
        when(actionService.getUncompletedAction(0L)).thenReturn(action);
        Knowledge knowledge = new Knowledge("test", "s", "a");
        when(knowledgeService.getBySubjectAndAttribute("test", "s", "a")).thenReturn(knowledge);
        when(knowledgeService.getResponse(knowledge)).thenReturn("response");
        when(agentService.checkDesireConstraints(1L, DesireName.D1)).thenReturn(false);

        agentService.reason(agent, new Perception("request", "s", "a", ""));
        verify(logEntryService).addLogEntry(new DesireUpdateLogEntry(false, DesireName.D1, agent));
    }

    @Test
    public void testSetAgentToPhaseValid() throws Exception {
        PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                Phase.PHASE2, Phase.PHASE3, Map.of(), Set.of(), Set.of(), new float[]{0, 1, 0.7f});
        when(constraintProvider.getPhaseTransitionConstraints(Phase.PHASE2)).thenReturn(
                constraints);

        Set<Belief> beliefs = Set.of(
                new Belief("B1", "Test Belief 1", 0.5f),
                new Belief("B2", "Test Belief 2", 0.7f),
                new Belief("B3", "Test Belief 3", 0.3f)
        );

        Agent agent = new Agent(1L, "testId", "test", beliefs, null, Phase.PHASE1, 0L,
                "", false, 0L, 0.0f, null, false, null);
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
        when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
        when(mockBeliefRepository.findByAgentIdAndName(1L, "B1"))
                .thenReturn(new Belief("B1", "Test Belief 1", 0.5f));
        when(mockBeliefRepository.findByAgentIdAndName(1L, "B2"))
                .thenReturn(new Belief("B2", "Test Belief 2", 0.7f));
        when(mockBeliefRepository.findByAgentIdAndName(1L, "B3"))
                .thenReturn(new Belief("B3", "Test Belief 3", 0.3f));

        when(mockConstraintService.checkDesireConstraints(any(), any())).thenReturn(true);

        Desire desireInactive = new Desire(0L, agent, "D1", "Desire number one", false,
                null);
        Desire desireActive = new Desire(0L, agent, "D1", "Desire number one", true,
                null);
        when(desireService.getByAgent(1L)).thenReturn(List.of(desireInactive));
        when(desireService.getActiveGoal(1L)).thenReturn(desireActive);
        when(beliefService.sortBeliefsByName(any())).thenReturn(List.of(
                new Belief("B1", "Test Belief 1", 0.5f),
                new Belief("B2", "Test Belief 2", 0.7f),
                new Belief("B3", "Test Belief 3", 0.3f)));

        List<BeliefChangeModel> result = agentService.setAgentStateToPhase("testId", Phase.PHASE2);

        assertThat(result).containsAll(Set.of(new BeliefChangeModel("B1", 0),
                new BeliefChangeModel("B2", 1), new BeliefChangeModel("B3", 0.7f)));
        verify(beliefService).setBeliefValue(agent, "B1",  0f);
        verify(beliefService).setBeliefValue(agent, "B2",  1f);
        verify(beliefService).setBeliefValue(agent, "B3",  0.7f);

        verify(logEntryService).addLogEntry(new DesireUpdateLogEntry(true, DesireName.D1, agent));
        desireInactive.setActive(true);
        verify(desireService).addDesire(desireInactive);
        verify(actionService).setActionsUncompleted(List.of(desireInactive));

        verify(messagingTemplate).convertAndSend("/topic/phase/testId",
                "{\"phaseFrom\":\"PHASE1\",\"phaseTo\":\"PHASE2\"}");

        // Should have been set to active again
        assertTrue(agent.isActive());
    }

    @Test
    public void testSetAgentToPhaseAgentDoesNotExist() {
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(false);

        assertThatThrownBy(() -> agentService.setAgentStateToPhase("testId", Phase.PHASE2))
                .isInstanceOf(EntityNotFoundException.class);

        verify(logEntryService, never()).addLogEntry(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    public void testSetAgentToPhaseAgentPhaseNull() {
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);

        assertThatThrownBy(() -> agentService.setAgentStateToPhase("testId", null))
                .isInstanceOf(NullPointerException.class);

        verify(logEntryService, never()).addLogEntry(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    public void testSetAgentToPhaseAgentSizeMisMatch() {
        PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                Phase.PHASE2, Phase.PHASE3, Map.of(), Set.of(), Set.of(), new float[]{0, 1});
        when(constraintProvider.getPhaseTransitionConstraints(Phase.PHASE2)).thenReturn(
                constraints);

        Set<Belief> beliefs = Set.of(new Belief("B1", "Test Belief 1", 0.5f));

        Agent agent = new Agent(1L, "testId", "test", beliefs, null, null, 0L,
                "", true, 0L, 0.0f, null, false, null);
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
        when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);

        assertThatThrownBy(() -> agentService.setAgentStateToPhase("testId", Phase.PHASE2))
                .isInstanceOf(SizeMismatchException.class);

        verify(logEntryService, never()).addLogEntry(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    public void testUpdatePhaseOfAgentNonNull() {
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L,
                "", true, 0L, 0.0f, null, false, null);

        when(mockConstraintService.checkDesireConstraints(any(), any())).thenReturn(true);
        Desire desireActive = new Desire(0L, agent, "D1", "Desire number one", true,
                null);
        when(desireService.getActiveGoal(1L)).thenReturn(desireActive);

        Phase result = agentService.updatePhaseOfAgent(agent);

        assertThat(result).isEqualTo(Phase.PHASE2);
        verify(mockAgentRepository).save(agent);
        assertThat(agent.getPhase()).isEqualTo(Phase.PHASE2);
    }

    @Test
    public void testUpdatePhaseOfAgentNull() {
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L,
                "", true, 0L, 0.0f, null, false, null);

        when(mockConstraintService.checkDesireConstraints(any(), any())).thenReturn(true);
        when(desireService.getActiveGoal(1L)).thenReturn(null);

        Phase result = agentService.updatePhaseOfAgent(agent);

        assertThat(result).isEqualTo(Phase.PHASE1);
        verify(mockAgentRepository).save(agent);
        assertThat(agent.getPhase()).isEqualTo(Phase.PHASE1);
    }

    @Test
    public void testUpdatePhaseOfAgentInvalid() {
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L,
                "", true, 0L, 0.0f, null, false, null);

        when(mockConstraintService.checkDesireConstraints(any(), any())).thenReturn(true);
        Desire desireActive = new Desire(0L, agent, "invalid", "Desire number one", true,
                null);
        when(desireService.getActiveGoal(1L)).thenReturn(desireActive);

        assertThatThrownBy(() -> agentService.updatePhaseOfAgent(agent)).isInstanceOf(IllegalStateException.class);

        assertThat(agent.getPhase()).isEqualTo(null);
    }

    @Test
    public void sendPhaseOfAgent() {
        assertTrue(agentService.sendPhaseOfAgent(Phase.PHASE1, Phase.PHASE2, true, "testId"));
        verify(messagingTemplate).convertAndSend("/topic/phase/testId",
                "{\"phaseFrom\":\"PHASE1\",\"phaseTo\":\"PHASE2\"}");

        assertTrue(agentService.sendPhaseOfAgent(Phase.PHASE1, Phase.PHASE2, false, "testId"));
        verify(messagingTemplate, times(2)).convertAndSend("/topic/phase/testId",
                "{\"phaseFrom\":\"PHASE1\",\"phaseTo\":\"PHASE2\"}");


        assertTrue(agentService.sendPhaseOfAgent(Phase.PHASE1, Phase.PHASE1, false, "testId"));
        verify(messagingTemplate).convertAndSend("/topic/phase/testId",
                "{\"phaseFrom\":\"PHASE1\",\"phaseTo\":\"PHASE1\"}");

        assertTrue(agentService.sendPhaseOfAgent(Phase.PHASE1, Phase.PHASE1, true, "testId"));
        verify(messagingTemplate, atMostOnce()).convertAndSend("/topic/phase/testId",
                "{\"phaseFrom\":\"PHASE1\",\"phaseTo\":\"PHASE1\"}");
        verify(messagingTemplate, times(2)).convertAndSend("/topic/phase/testId",
                "{\"phaseFrom\":\"PHASE1\",\"phaseTo\":\"PHASE2\"}");
    }

    @Test
    public void setAgentActiveValid() {
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L,
                "", false, 0L, 0.0f, null, false, null);
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
        when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);

        // Different value sets value
        agentService.setAgentActive("testId", true);
        assertTrue(agent.isActive());
        verify(mockAgentRepository).save(agent);

        // Same value does not influence
        agentService.setAgentActive("testId", true);
        assertTrue(agent.isActive());
        verify(mockAgentRepository, times(2)).save(agent);
    }

    @Test
    public void setAgentActiveInvalid() {
        when(mockAgentRepository.existsByUserId("testId")).thenReturn(false);
        assertThatThrownBy(() -> agentService.setAgentActive("testId", true))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
