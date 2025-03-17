package com.bdi.agent.api;

import com.bdi.agent.model.api.BeliefChangeModel;
import com.bdi.agent.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin(origins = "*")
public class WebSocketBeliefController {


    private final AgentService agentService;

    /**
     * Instantiates a new Belief Controller.
     *
     * @param agentService the agentService for accessing agents.
     */
    @Autowired
    public WebSocketBeliefController(AgentService agentService) {
        this.agentService = agentService;
    }

    /** Takes the message that was received from the front-end and updates the agent's belief value accordingly. Used
     * when a trainer changes a belief value manually.
     *
     * @param message the message in the form of a BeliefChangeModel
     * @param sessionId the ID of the session the belief belongs to.
     */
    @MessageMapping("/update/{sessionId}")
    public void forwardMessage(BeliefChangeModel message, @DestinationVariable String sessionId) {
        try {
            agentService.updateBelief(sessionId, message.getBelief(), message.getValue());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}