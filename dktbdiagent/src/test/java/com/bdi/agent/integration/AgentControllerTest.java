package com.bdi.agent.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.bdi.agent.api.AgentController;
import com.bdi.agent.model.Agent;
import com.bdi.agent.repository.AgentRepository;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations="classpath:application-test.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgentRepository agentRepository;

    @Test
    @WithMockUser(username = "test", password = "test", roles = "USER")
    public void startSessionUserDoesntExist() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/create/testId"));

        MvcResult result =  resultActions.andExpect(status().isOk())
                .andExpect(handler().methodCall(on(AgentController.class).startSession("testId", null)))
                .andReturn();

        assertTrue(agentRepository.existsByUserId("testId"));
        assertEquals("Session has been created", result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(username = "test", password = "test", roles = "USER")
    public void startSessionUserExists() throws Exception {
        agentRepository.save(new Agent(1L, "testId", "test", null, null, null, 1L, "", true, 1L, 1f, null, true, null));
        ResultActions resultActions = mockMvc.perform(post("/create/testId"));

        MvcResult result =  resultActions.andExpect(status().isOk())
                .andExpect(handler().methodCall(on(AgentController.class).startSession("testId", null)))
                .andReturn();

        assertTrue(agentRepository.existsByUserId("testId"));
        assertEquals("Session has been created", result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(username = "test", password = "test", roles = "USER")
    public void changeModeUserDoesntExist() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/agent/changeMode/testId")
                .contentType("application/json").content("true"));

        MvcResult result = resultActions.andExpect(status().isNotFound())
                .andExpect(handler().methodCall(on(AgentController.class).changeAgentMode("testId", true)))
                .andReturn();

        assertEquals("Session not found", result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(username = "test", password = "test", roles = "USER")
    public void changeModeUserExists() throws Exception {
        agentRepository.save(new Agent(1L, "testId", "test", null, null, null, 1L, "", true,
                1L, 1f, null, false, null));
        ResultActions resultActions = mockMvc.perform(post("/agent/changeMode/testId")
                .contentType("application/json").content("true"));

        MvcResult result = resultActions.andExpect(status().isOk())
                .andExpect(handler().methodCall(on(AgentController.class).changeAgentMode("testId", true)))
                        .andReturn();

        assertEquals("Agent trainer mode changed to true", result.getResponse().getContentAsString());
        assertEquals(true, agentRepository.getByUserId("testId").isTrainerResponding());
    }

    @Test
    @WithMockUser(username = "test", password = "test", roles = "USER")
    public void changeModeUserExistsTrue() throws Exception {
        agentRepository.save(new Agent(1L, "testId", "test", null, null,
                null, 1L, "", true, 1L, 1f, null, true, null));
        ResultActions resultActions = mockMvc.perform(post("/agent/changeMode/testId")
                .contentType("application/json").content("false"));

        MvcResult result = resultActions.andExpect(status().isOk())
                .andExpect(handler().methodCall(on(AgentController.class).changeAgentMode("testId", true)))
                .andReturn();

        assertEquals("Agent trainer mode changed to false", result.getResponse().getContentAsString());
        assertEquals(false, agentRepository.getByUserId("testId").isTrainerResponding());
    }

}
