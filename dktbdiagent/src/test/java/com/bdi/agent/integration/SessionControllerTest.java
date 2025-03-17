package com.bdi.agent.integration;

import com.bdi.agent.model.Agent;
import com.bdi.agent.service.AgentService;
import com.bdi.agent.service.LogEntryService;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({ "mockAgentService", "mockAgentRepository" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class SessionControllerTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private AgentService mockAgentService;

    @MockBean
    private LogEntryService logEntryServiceMock;

    @Test
    void testTrainerMessage() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketStompClient webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        Agent agent = new Agent(1L, "testId", "test", null, null, null, 1L, "", true, 1L, 1f, new ArrayList<>(), false,
                null);

        when(mockAgentService.getByUserId("sessionId")).thenReturn(agent);

        StompSession session = webSocketStompClient.connect("ws://localhost:" + port + "/session",
                new StompSessionHandlerAdapter() {
                }).get(1, SECONDS);
        List<String> messages = List.of("Hello", "World");
        session.send("/app/trainer/sessionId", messages);

        verify(logEntryServiceMock, timeout(1000)).addMessageLogEntry(eq("Hello"), eq(false), eq(agent));
    }

}
