package com.bdi.agent.integration;

import com.bdi.agent.api.WebSocketBeliefController;
import com.bdi.agent.model.api.BeliefChangeModel;
import com.bdi.agent.service.AgentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WebSocketBeliefTest {

    @Mock
    private AgentService agentService;

    @InjectMocks
    private WebSocketBeliefController webSocketBeliefController;

    @Captor
    private ArgumentCaptor<String> sessionIdCaptor;

    @Captor
    private ArgumentCaptor<String> beliefCaptor;

    @Captor
    private ArgumentCaptor<Float> valueCaptor;

    public WebSocketBeliefTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testForwardMessage() {
        String sessionId = "testId";
        String belief = "B1";
        float value = 0.5f;

        BeliefChangeModel message = new BeliefChangeModel(belief, value);

        webSocketBeliefController.forwardMessage(message, sessionId);

        verify(agentService).updateBelief(
                sessionIdCaptor.capture(),
                beliefCaptor.capture(),
                valueCaptor.capture()
        );

        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(belief, beliefCaptor.getValue());
        assertEquals(value, valueCaptor.getValue());
    }
}
