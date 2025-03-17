package com.bdi.agent.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDateTime;
import java.util.List;

import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.bdi.agent.api.OptimalPathController;
import com.bdi.agent.authorization.JwtTokenUtils;
import com.bdi.agent.authorization.JwtUserDetailsService;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Conversation;
import com.bdi.agent.model.Role;
import com.bdi.agent.model.User;
import com.bdi.agent.model.api.OptimalPathModel;
import com.bdi.agent.model.dto.MessageNodeDto;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.repository.AgentRepository;
import com.bdi.agent.service.UserService;
import com.bdi.agent.service.graph.OptimalPathService;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({ "mockOptimalPathService" })
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
public class OptimalPathControllerTest {
        /*
         * Note: This is not really an integration test, but rather a unit test. This
         * stems from the complexity of the
         * optimal path generation, and that therefore the generated path cannot be
         * easily asserted on.
         * This is why the service is mocked, and it is only asserted that the correct
         * result is returned/forwarded
         * at the endpoint.
         */

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private JwtTokenUtils jwtTokenUtils;

        @MockBean
        private JwtUserDetailsService mockUserDetailsService;
        @MockBean
        private AgentRepository agentRepository;
        @MockBean
        private UserService userService;

        private transient final OptimalPathService mockedPathService;
        private final User trainer = new User("username", "password", "email", Role.TRAINER);

        @Autowired
        public OptimalPathControllerTest(OptimalPathService mockedPathService) {
                this.mockedPathService = mockedPathService;
        }

        @Test
        @WithMockUser(username = "test", password = "test", roles = "USER")
        public void testGetOptimalPath() throws Exception {

                Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L,
                                "", true, 0L, 0.0f, null,
                                false, null);
                User learner = new User("learner", "password", "email2", Role.LEARNER);
                Conversation conversation = new Conversation("Name", LocalDateTime.now(), agent, learner);
                agent.setConversation(conversation);

                when(agentRepository.findAll()).thenReturn(List.of(agent));
                when(userService.getUsersFromConversationId(conversation.getConversationId()))
                                .thenReturn(List.of(learner));
                when(userService.isAssignedTrainer(learner.getUsername(), trainer.getUsername())).thenReturn(true);

                MessageNodeDto messageNodeDto = new MessageNodeDto(List.of(), List.of(), Phase.PHASE1, null);
                when(mockedPathService.generateOptimalPathRequest(any())).thenReturn(new OptimalPathModel(
                                List.of(messageNodeDto)));
                when(mockUserDetailsService.loadUserByUsername(trainer.getUsername())).thenReturn(trainer);

                String token = jwtTokenUtils.generateToken(trainer);

                mockMvc.perform(get("/optimal-path/testId").header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(handler().methodCall(on(OptimalPathController.class)
                                                .getOptimalPath("testId", HttpHeaders.AUTHORIZATION)))
                                .andExpect(jsonPath("$.nodes", hasSize(1)))
                                .andExpect(jsonPath("$.nodes[0].beliefs", hasSize(0)))
                                .andExpect(jsonPath("$.nodes[0].desires", hasSize(0)))
                                .andExpect(jsonPath("$.nodes[0].phase").value("PHASE1"))
                                .andExpect(jsonPath("$.nodes[0].edge").value(IsNull.nullValue()));
        }
}
