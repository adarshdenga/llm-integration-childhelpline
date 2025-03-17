package com.bdi.agent.integration;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.api.BeliefChangeClientModel;
import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BeliefUpdateType;
import com.bdi.agent.model.util.BeliefUpdateLogEntry;
import com.bdi.agent.model.util.MessageLogEntry;
import com.bdi.agent.service.BeliefService;
import com.bdi.agent.service.LogEntryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class LiveBeliefsTest {

        @LocalServerPort
        private Integer port;

        @Autowired
        private BeliefService beliefService;

        @MockBean
        private LogEntryService logEntryService;

        @Test
        void testLiveBeliefs() throws ExecutionException, InterruptedException, TimeoutException {

                // Setup
                WebSocketStompClient webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());

                BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

                webSocketStompClient.setMessageConverter(new StringMessageConverter());

                StompSession session = webSocketStompClient.connect("ws://localhost:" + port + "/session",
                                new StompSessionHandlerAdapter() {
                                }).get(1, SECONDS);

                session.subscribe("/topic/beliefs/testId", new StompFrameHandler() {

                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                                return String.class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                                blockingQueue.add((String) payload);
                        }

                });

                Thread.sleep(1000);

                Agent agent = new Agent();
                agent = new Agent(1L, "testId", "test", null, null, null, 0L, "",
                                true, 0L, 0.0f, List.of(new MessageLogEntry("test message",
                                                true, agent)),
                                false, null);

                when(logEntryService.getUserMessageLogsByAgentChronologicalUntilTimestamp(
                                eq(1L), any()))
                                .thenReturn(List.of(new MessageLogEntry("test message",
                                                true, agent)));

                // Action
                beliefService.sendBeliefsToClientAndLog(agent,
                                List.of(new BeliefUpdateLogEntry(BeliefUpdateType.SET_TO, 0.3f,
                                                BeliefName.B1, "test message", agent, false)));

                // Test
                await()
                                .atMost(10, SECONDS)
                                .untilAsserted(() -> assertEquals(
                                                new BeliefChangeClientModel("B1", 0.3f, "test message",
                                                                0, false, BeliefUpdateType.SET_TO),
                                                new ObjectMapper().readValue(blockingQueue.poll(1, SECONDS),
                                                                BeliefChangeClientModel.class)));
        }
}
