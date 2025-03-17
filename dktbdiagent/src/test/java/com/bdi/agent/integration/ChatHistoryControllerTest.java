package com.bdi.agent.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.bdi.agent.api.ChatHistoryController;
import com.bdi.agent.authorization.JwtTokenUtils;
import com.bdi.agent.authorization.JwtUserDetailsService;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Conversation;
import com.bdi.agent.model.Role;
import com.bdi.agent.model.User;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.service.ConversationService;
import com.bdi.agent.service.UserService;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ChatHistoryControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserService userService;

        @MockBean
        private ConversationService conversationService;

        @MockBean
        private JwtTokenUtils jwtTokenUtils;

        @MockBean
        private JwtUserDetailsService jwtUserDetailsService;

        @Test
        @WithMockUser(username = "j_doe", password = "1VeryUnsafePassword!", roles = "LEARNER")
        public void getChatHistoryMetadataListTest() throws Exception {
                Agent agent = new Agent(1L, "testId", "test", null, null, Phase.PHASE1, 1L, "", true, 1L, 1f, null,
                                false, null);
                User user = new User(1L, "j_doe", "1VeryUnsafePassword!", "j.doe@mail.com", Role.LEARNER, null, null,
                                0);
                Conversation conversation = new Conversation(1L, "Name", null, agent, user, "");

                Mockito.when(userService.getByUsername("j_doe"))
                                .thenReturn(user);

                Mockito.when(conversationService.getAllConversationsByUserId(1L))
                                .thenReturn(List.of(conversation));

                Mockito.when(jwtUserDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);

                Mockito.when(jwtTokenUtils.retrieveUsernameFromToken(any()))
                                .thenReturn(user.getUsername());
                Mockito.when(jwtTokenUtils.getUsername("token")).thenReturn(user.getUsername());

                ResultActions resultActions = mockMvc.perform(get("/history/all")
                                .header("Authorization", "Bearer token"));

                MvcResult result = resultActions.andExpect(status().isOk())
                                .andExpect(handler().methodCall(
                                                on(ChatHistoryController.class).getChatHistoryMetaDataByUser("j_doe")))
                                .andReturn();

                assertEquals("[{\"conversationId\":1,\"title\":\"Name\",\"timestamp\":null}]",
                                result.getResponse().getContentAsString());
        }
}
