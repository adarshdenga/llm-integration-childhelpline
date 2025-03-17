package com.bdi.agent.integration;

import com.bdi.agent.TestUtils;
import com.bdi.agent.api.BeliefController;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Belief;
import com.bdi.agent.model.api.BeliefChangeModel;
import com.bdi.agent.model.api.PhaseChangeRequest;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.util.PhaseTransitionConstraints;
import com.bdi.agent.repository.AgentRepository;
import com.bdi.agent.repository.BeliefRepository;
import com.bdi.agent.utils.ConstraintProvider;
import com.bdi.agent.service.AgentService;
import com.bdi.agent.service.LogEntryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({ "mockBeliefRepository", "mockAgentRepository", "mockSimpMessagingTemplate",
                "mockConstraintProvider" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
public class BeliefTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private transient BeliefRepository mockBeliefRepository;

        @Autowired
        private transient AgentRepository mockAgentRepository;

        @Autowired
        private transient ConstraintProvider constraintProvider;

        @Autowired
        private transient TestUtils testUtils;

        @MockBean
        private LogEntryService logEntryService; // needed

        @Autowired
        private AgentService agentService;

        @Test
        public void updateBeliefInvalidValue() throws Exception {
                BeliefChangeModel model = new BeliefChangeModel("B1", -0.1f);
                String modelSerialized = new ObjectMapper().writeValueAsString(model);

                ResultActions result = mockMvc.perform(put("/beliefs/update/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(modelSerialized));

                result.andExpect(status().isBadRequest())
                                .andExpect(handler()
                                                .methodCall(on(BeliefController.class).updateBelief("testId", model)));
        }

        @Test
        public void updateBeliefInvalidValueLarger() throws Exception {
                BeliefChangeModel model = new BeliefChangeModel("B1", 1.1f);
                String modelSerialized = new ObjectMapper().writeValueAsString(model);

                ResultActions result = mockMvc.perform(put("/beliefs/update/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(modelSerialized));

                result.andExpect(status().isBadRequest())
                                .andExpect(handler()
                                                .methodCall(on(BeliefController.class).updateBelief("testId", model)));
        }

        @Test
        public void updateBeliefInvalidAgent() throws Exception {
                BeliefChangeModel model = new BeliefChangeModel("B1", 0.5f);
                String modelSerialized = new ObjectMapper().writeValueAsString(model);
                when(mockAgentRepository.existsByUserId("testId")).thenReturn(false);

                ResultActions result = mockMvc.perform(put("/beliefs/update/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(modelSerialized));

                result.andExpect(status().isBadRequest())
                                .andExpect(handler()
                                                .methodCall(on(BeliefController.class).updateBelief("testId", model)));
        }

        @Test
        public void updateBeliefInvalidBelief() throws Exception {
                BeliefChangeModel model = new BeliefChangeModel("B1", 0.5f);
                String modelSerialized = new ObjectMapper().writeValueAsString(model);
                Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L,
                                "", true, 0L, 0.0f, null, false, null);
                when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
                when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B1")).thenReturn(null);

                ResultActions result = mockMvc.perform(put("/beliefs/update/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(modelSerialized));

                result.andExpect(status().isBadRequest())
                                .andExpect(handler()
                                                .methodCall(on(BeliefController.class).updateBelief("testId", model)));
        }

        @Test
        public void updateBeliefValid() throws Exception {
                BeliefChangeModel model = new BeliefChangeModel("B1", 0.5f);
                String modelSerialized = new ObjectMapper().writeValueAsString(model);
                Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true, 0L, 0.0f, null, false,
                                null);
                Belief b1 = new Belief(0L, agent, "B1", "", "", 0.3f);
                Belief b3 = new Belief(0L, agent, "B3", "", "", 0.3f);
                Belief b4 = new Belief(0L, agent, "B4", "", "", 0.3f);
                Belief b5 = new Belief(0L, agent, "B5", "", "", 0.3f);
                Belief b6 = new Belief(0L, agent, "B6", "", "", 0.3f);
                Belief b7 = new Belief(0L, agent, "B7", "", "", 0.3f);
                when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
                when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B1")).thenReturn(b1);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B3")).thenReturn(b3);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B4")).thenReturn(b4);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B5")).thenReturn(b5);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B6")).thenReturn(b6);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B7")).thenReturn(b7);
                ReflectionTestUtils.setField(agentService, "relatednessBeliefs",
                                new String[] { "B4", "B5", "B6", "B7" }, String[].class);

                ResultActions result = mockMvc.perform(put("/beliefs/update/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(modelSerialized));

                result.andExpect(status().isOk())
                                .andExpect(handler()
                                                .methodCall(on(BeliefController.class).updateBelief("testId", model)));
                ArgumentCaptor<Belief> argumentCaptor = ArgumentCaptor.forClass(Belief.class);
                verify(mockBeliefRepository).save(argumentCaptor.capture());

                assertEquals("B1", argumentCaptor.getValue().getName());
                assertEquals(0.5f, argumentCaptor.getValue().getValue());
        }

        @Test
        public void updateBeliefValidChangingRelatednessBeliefs() throws Exception {
                BeliefChangeModel model = new BeliefChangeModel("B4", 0.9f);
                String modelSerialized = new ObjectMapper().writeValueAsString(model);
                Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true, 0L, 0.0f, null, false,
                                null);
                Belief b1 = new Belief(0L, agent, "B1", "", "", 0.3f);
                Belief b3 = new Belief(0L, agent, "B3", "", "", 0.3f);
                Belief b4 = new Belief(0L, agent, "B4", "", "", 0.3f);
                Belief b5 = new Belief(0L, agent, "B5", "", "", 0.3f);
                Belief b6 = new Belief(0L, agent, "B6", "", "", 0.3f);
                Belief b7 = new Belief(0L, agent, "B7", "", "", 0.3f);
                when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
                when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B1")).thenReturn(b1);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B3")).thenReturn(b3);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B4")).thenReturn(b4);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B5")).thenReturn(b5);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B6")).thenReturn(b6);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B7")).thenReturn(b7);
                ReflectionTestUtils.setField(agentService, "relatednessBeliefs",
                                new String[] { "B4", "B5", "B6", "B7" }, String[].class);

                ResultActions result = mockMvc.perform(put("/beliefs/update/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(modelSerialized));

                result.andExpect(status().isOk())
                                .andExpect(handler()
                                                .methodCall(on(BeliefController.class).updateBelief("testId", model)));
                ArgumentCaptor<Belief> argumentCaptor = ArgumentCaptor.forClass(Belief.class);
                verify(mockBeliefRepository, times(2)).save(argumentCaptor.capture());

                List<Belief> savedBeliefs = argumentCaptor.getAllValues();
                assertEquals("B4", savedBeliefs.get(0).getName());
                assertEquals(0.9f, savedBeliefs.get(0).getValue());
                assertEquals("B3", savedBeliefs.get(1).getName());
                assertEquals(0.45f, savedBeliefs.get(1).getValue());
        }

        @Test
        void testGetAllBeliefsValid() throws Exception {
                Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L,
                                "", true, 0L, 0.0f, null, false, null);

                Set<Belief> beliefs = Set.of(
                                new Belief("B1", "Test Belief 1", 0.5f),
                                new Belief("B2", "Test Belief 2", 0.7f),
                                new Belief("B3", "Test Belief 3", 0.3f));

                when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
                when(mockBeliefRepository.findByAgentIdOrderByPhaseAsc(1L)).thenReturn(beliefs);

                mockMvc.perform(get("/beliefs/all/testId"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[*].belief").value(Matchers.containsInAnyOrder("B1", "B2", "B3")))
                                .andExpect(jsonPath("$[?(@.belief == 'B1')].value").value(0.5))
                                .andExpect(jsonPath("$[?(@.belief == 'B2')].value").value(0.7))
                                .andExpect(jsonPath("$[?(@.belief == 'B3')].value").value(0.3));
        }

        @Test
        public void testGetAllBeliefsInvalid() throws Exception {
                BeliefChangeModel model = new BeliefChangeModel("B1", 0.5f);
                String modelSerialized = new ObjectMapper().writeValueAsString(model);
                when(mockAgentRepository.getByUserId("testId")).thenReturn(null);

                ResultActions result = mockMvc.perform(get("/beliefs/all/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(modelSerialized));

                result.andExpect(status().isBadRequest())
                                .andExpect(handler().methodCall(on(BeliefController.class).getAllBeliefs("testId")));
        }

        @Test
        public void testChangeAgentToPhaseValid() throws Exception {
                PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                                Phase.PHASE2, Phase.PHASE3, Map.of(), Set.of(), Set.of(), new float[] { 0, 1, 0.7f });
                when(constraintProvider.getPhaseTransitionConstraints(Phase.PHASE2)).thenReturn(
                                constraints);

                Set<Belief> beliefs = Set.of(
                                new Belief("B1", "Test Belief 1", 0.5f),
                                new Belief("B2", "Test Belief 2", 0.7f),
                                new Belief("B3", "Test Belief 3", 0.3f));

                Agent agent = new Agent(1L, "testId", "test", beliefs, null, null, 0L,
                                "", true, 0L, 0.0f, null, false, null);
                when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
                when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B1"))
                                .thenReturn(new Belief("B1", "Test Belief 1", 0.5f));
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B2"))
                                .thenReturn(new Belief("B2", "Test Belief 2", 0.7f));
                when(mockBeliefRepository.findByAgentIdAndName(1L, "B3"))
                                .thenReturn(new Belief("B3", "Test Belief 3", 0.3f));

                ObjectMapper objectMapper = new ObjectMapper();
                String result = mockMvc.perform(put("/beliefs/phase")
                                .content(objectMapper
                                                .writeValueAsString(new PhaseChangeRequest("testId", Phase.PHASE2)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

                BeliefChangeModel[] bodyResult = objectMapper.readValue(result, BeliefChangeModel[].class);

                assertThat(bodyResult).containsAll(Set.of(new BeliefChangeModel("B1", 0),
                                new BeliefChangeModel("B2", 1), new BeliefChangeModel("B3", 0.7f)));
                verify(mockBeliefRepository).save(new Belief("B1", "Test Belief 1", 0f));
                verify(mockBeliefRepository).save(new Belief("B2", "Test Belief 2", 1f));
                verify(mockBeliefRepository).save(new Belief("B3", "Test Belief 3", 0.7f));
        }

        @Test
        public void testChangeAgentToPhaseAgentDoesNotExist() throws Exception {
                when(mockAgentRepository.existsByUserId("testId")).thenReturn(false);
                when(mockAgentRepository.getByUserId("testId")).thenReturn(null);

                ObjectMapper objectMapper = new ObjectMapper();
                mockMvc.perform(put("/beliefs/phase")
                                .content(objectMapper
                                                .writeValueAsString(new PhaseChangeRequest("testId", Phase.PHASE2)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest()).andExpect(content().string("Agent not found"));

                verify(mockBeliefRepository, never()).save(any());
        }

        @Test
        public void testChangeAgentToPhaseAgentPhaseNull() throws Exception {
                Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L,
                                "", true, 0L, 0.0f, null, false, null);
                when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
                when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);

                ObjectMapper objectMapper = new ObjectMapper();
                mockMvc.perform(put("/beliefs/phase")
                                .content(objectMapper.writeValueAsString(new PhaseChangeRequest("testId", null)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest()).andExpect(content().string("Phase cannot be null"));

                verify(mockBeliefRepository, never()).save(any());
        }

        @Test
        public void testChangeAgentToPhaseAgentSizeMismatch() throws Exception {
                PhaseTransitionConstraints constraints = new PhaseTransitionConstraints(
                                Phase.PHASE2, Phase.PHASE3, Map.of(), Set.of(), Set.of(), new float[] { 0, 1 });
                when(constraintProvider.getPhaseTransitionConstraints(Phase.PHASE2)).thenReturn(
                                constraints);

                Set<Belief> beliefs = Set.of(new Belief("B1", "Test Belief 1", 0.5f));

                Agent agent = new Agent(1L, "testId", "test", beliefs, null, null, 0L,
                                "", true, 0L, 0.0f, null, false, null);
                when(mockAgentRepository.existsByUserId("testId")).thenReturn(true);
                when(mockAgentRepository.getByUserId("testId")).thenReturn(agent);

                ObjectMapper objectMapper = new ObjectMapper();
                mockMvc.perform(put("/beliefs/phase")
                                .content(objectMapper.writeValueAsString(new PhaseChangeRequest("testId",
                                                Phase.PHASE2)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest()).andExpect(content().string("Belief set size 1 does "
                                                + "not match size 2"));

                verify(mockBeliefRepository, never()).save(any());
        }
}
