package com.bdi.agent.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Perception;
import com.bdi.agent.model.util.MessageLogEntry;
import com.bdi.agent.repository.AgentRepository;
import com.bdi.agent.service.AgentService;
import com.bdi.agent.service.LogEntryService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MessageLoggingTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private AgentRepository agentRepository;

        @MockBean
        private AgentService agentServiceMock;

        @MockBean
        private LogEntryService logEntryServiceMock;

        @Test
        @WithMockUser(username = "test", password = "test", roles = "USER")
        public void addPerceptionWhenNotTrainerRespondingTest_Trigger() throws Exception {
                Agent agent = new Agent(1L, "testId", "test", null, null, null, 1L, "", true, 1L, 1f, new ArrayList<>(),
                                false,
                                null);
                agentRepository.save(agent);

                String expectedResponse = "response";
                Mockito.when(agentServiceMock.reason(any(Agent.class), any(Perception.class)))
                                .thenReturn(expectedResponse);

                Mockito.when(agentServiceMock.getByUserId("testId"))
                                .thenReturn(agent);

                Perception perception = new Perception("trigger", "s", "a", "");
                String perceptionSerialized = new ObjectMapper().writeValueAsString(perception);

                mockMvc.perform(post("/agent/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(perceptionSerialized));

                verify(agentServiceMock).reason(agent, perception);

                verify(logEntryServiceMock).addLogEntry(new MessageLogEntry("response", false, agent, null));

        }

        @Test
        @WithMockUser(username = "test", password = "test", roles = "USER")
        public void addPerceptionWhenTrainerRespondingTest_Trigger() throws Exception {
                Agent agent = new Agent(1L, "testId", "test", null, null, null, 1L, "", true, 1L, 1f, new ArrayList<>(),
                                true,
                                null);
                agentRepository.save(agent);

                String expectedResponse = "response";
                Mockito.when(agentServiceMock.reason(any(Agent.class), any(Perception.class)))
                                .thenReturn(expectedResponse);

                Mockito.when(agentServiceMock.getByUserId("testId"))
                                .thenReturn(agent);

                Perception perception = new Perception("trigger", "s", "a", "");
                String perceptionSerialized = new ObjectMapper().writeValueAsString(perception);

                mockMvc.perform(post("/agent/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(perceptionSerialized));

                verify(agentServiceMock).reason(agent, perception);

                verifyNoMoreInteractions(logEntryServiceMock);
        }

        @Test
        @WithMockUser(username = "test", password = "test", roles = "USER")
        public void addPerceptionWhenNotTrainerRespondingTest_Request() throws Exception {
                Agent agent = new Agent(1L, "testId", "test", null, null, null, 1L, "", true, 1L, 1f, new ArrayList<>(),
                                false,
                                null);
                agentRepository.save(agent);

                String expectedResponse = "response";
                Mockito.when(agentServiceMock.reason(any(Agent.class), any(Perception.class)))
                                .thenReturn(expectedResponse);

                Mockito.when(agentServiceMock.getByUserId("testId"))
                                .thenReturn(agent);

                Perception perception = new Perception("request", "s", "a", "");
                String perceptionSerialized = new ObjectMapper().writeValueAsString(perception);

                mockMvc.perform(post("/agent/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(perceptionSerialized));

                verify(agentServiceMock).reason(agent, perception);

                verify(logEntryServiceMock).addLogEntry(new MessageLogEntry("response", false, agent, null));
        }

        @Test
        @WithMockUser(username = "test", password = "test", roles = "USER")
        public void addPerceptionWhenTrainerRespondingTest_Request() throws Exception {
                Agent agent = new Agent(1L, "testId", "test", null, null, null, 1L, "", true, 1L, 1f, new ArrayList<>(),
                                true, null);
                agentRepository.save(agent);

                String expectedResponse = "response";
                Mockito.when(agentServiceMock.reason(any(Agent.class), any(Perception.class)))
                                .thenReturn(expectedResponse);

                Mockito.when(agentServiceMock.getByUserId("testId"))
                                .thenReturn(agent);

                Perception perception = new Perception("request", "s", "a", "");
                String perceptionSerialized = new ObjectMapper().writeValueAsString(perception);

                mockMvc.perform(post("/agent/testId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(perceptionSerialized));

                verify(agentServiceMock).reason(agent, perception);

                verifyNoMoreInteractions(logEntryServiceMock);
        }

}
