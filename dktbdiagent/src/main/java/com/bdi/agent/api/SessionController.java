package com.bdi.agent.api;

import static java.lang.String.format;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.api.MessageModel;
import com.bdi.agent.service.AgentService;
import com.bdi.agent.service.ConversationService;
import com.bdi.agent.service.LogEntryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Controller
@CrossOrigin(origins = "*")
public class SessionController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private final AgentService agentService;

    @Autowired
    private final LogEntryService logEntryService;

    @Autowired
    private final ConversationService conversationService;

    private Map<String, Integer> subscriberCounts = new ConcurrentHashMap<>();


    /**
     * Instantiates a new Session Controller.
     *
     * @param agentService the agentService for accessing agents.
     * @param logEntryService the logEntryService for accessing log entries.
     */
    @Autowired
    public SessionController(AgentService agentService,
                             LogEntryService logEntryService,
                             ConversationService conversationService) {
        this.agentService = agentService;
        this.logEntryService = logEntryService;
        this.conversationService = conversationService;
    }

    /**
     * Forwards a message to everyone involved in some training session.
     *
     * @param message the message to forward
     * @param sessionId the sessionId of the training session
     */
    @MessageMapping("/session/{sessionId}")
    public void forwardMessage(@RequestBody MessageModel message, @DestinationVariable String sessionId) {

        Agent agent = agentService.getByUserId(sessionId);

        logEntryService.addMessageLogEntry(message.getMessage(), message.isFromUser(), agent);
        messagingTemplate.convertAndSend(format("/topic/session/%s", sessionId),
                List.of(message));
    }

    /**
     * This endpoint is used to send a message from the trainer to the learner.
     * This is used when the trainer is manually sending messages.
     * Messages are sent as a list of strings to allow for multiple messages to be sent simultaneously.
     * This happens when the trainer turns on autosending with multiple pending messages.
     *
     * @param messagesToSend the messages from the trainer (replaces Lilobot messages)
     * @param sessionId the session id of the session/agent
     */
    @MessageMapping("/trainer/{sessionId}")
    public void trainerMessage(@RequestBody List<String> messagesToSend, @DestinationVariable String sessionId) {
        List<MessageModel> messageModels = new ArrayList<>();
        for (String message : messagesToSend) {
            Agent agent = agentService.getByUserId(sessionId);
            logEntryService.addMessageLogEntry(message, false, agent);
            messageModels.add(new MessageModel(message, false));
        }
        messagingTemplate.convertAndSend(format("/topic/session/%s", sessionId), messageModels);
    }

    /**
     * Sends a request to join a session to the user with the given username.
     *
     * @param message the username of the user sending the request
     * @param username the username of the user to send the request to
     */
    @MessageMapping("/session/join/{username}")
    public void requestSessionJoin(String message, @DestinationVariable String username) {
        messagingTemplate.convertAndSend(format("/topic/session/join/%s", username), message);
    }

    /**
     * Is used to reply to a session join request. This currently prompts the trainer to join the learner session.
     * In the future, a session id will be returned to the learner, which can be used to join the session.
     *
     * @param message if the request was accepted and sessionId of the session to join if accepted
     * @param userId user whose request is accepted
     */
    @MessageMapping("/session/accept/{userId}")
    public void acceptSessionJoin(String message, @DestinationVariable String userId) {
        messagingTemplate.convertAndSend(format("/topic/session/accept/%s", userId), message);
    }

    /**
     * When a learner assigns or removes a trainer, a message is sent to that trainer.
     *
     * @param message message containing the learner's username
     * @param username name of the trainer
     */
    @MessageMapping("/session/trainer_assign/{username}")
    public void trainerAssignmentNotification(String message, @DestinationVariable String username) {
        messagingTemplate.convertAndSend(format("/topic/session/trainer_assign/%s", username), message);
    }

    /**
     * Sends the phase of the agent to the subscription.
     *
     * @param userId user id for the agent
     */
    @MessageMapping("/phase/{userId}")
    public void sendPhaseOfAgent(@DestinationVariable String userId) {
        Agent agent = agentService.getByUserId(userId);
        agentService.sendPhaseOfAgent(agent.getPhase(), agentService.updatePhaseOfAgent(agent), false,
                agent.getUserId());
    }

    /**
     * Keeps track of how many clients have subscribed to each session topic. These are the topics that send messages
     * between Lilo and the learner. Keeping track of the total number of subscribers for each ongoing session allows
     * us to detect when the session is prematurely closed, rather than ending it by saying goodbye. This is important
     * for deleting empty conversations that often occur because conversations are created at mount time of the chat
     * webpage.
     *
     * @param event the session subscribe event
     */
    @EventListener
    public void handleSubscriptionEvent(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String subscriptionId = headerAccessor.getSubscriptionId();

        if (subscriptionId != null && isSessionTopic(subscriptionId)) {
            String sessionId = extractSessionId(subscriptionId);
            subscriberCounts.put(sessionId, subscriberCounts.getOrDefault(sessionId, 0) + 1);
        }
    }

    /**
     * Keeps track of unsubscribe events for the sessions we want to keep track of. By combining this method with
     * handleSubscriptionEvent, we know when a session has no more connected users. This is when our counter reaches
     * 0. When this happens, we check if the conversation is empty and, if that is the case, delete every conversation
     * that is connected to that session.
     *
     * @param event the session unsubscribe event
     */
    @EventListener
    public void handleUnsubscriptionEvent(SessionUnsubscribeEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String subscriptionId = headerAccessor.getSubscriptionId();

        if (subscriptionId != null && isSessionTopic(subscriptionId)) {
            String sessionId = extractSessionId(subscriptionId);
            subscriberCounts.put(sessionId, subscriberCounts.getOrDefault(sessionId, 0) - 1);

            if (subscriberCounts.get(sessionId) <= 0 && agentService.getConversation(sessionId).isEmpty()) {
                conversationService.deleteAllConversationsBySessionId(sessionId);
            }
        }
    }

    /**
     * Extracts the sessionId from the destination path of a STOMP subscription. This method assumes that the sessionId
     * resides at the last position of the path, so everything after the last '/'.
     *
     * @param destination the STOMP subscription destination path
     * @return the sessionId, or null if there is no sessionId
     */
    private String extractSessionId(String destination) {
        String[] parts = destination.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }

    /**
     * Checks if the destination belongs to a subscription for sending messages between Lilo and the learner. This
     * method assumes the destination for this will look like "/topic/session/{sessionId}", and that all other paths
     * starting with "/topic/session/" have at least 1 more part in their path.
     *
     * @param destination the STOMP subscription destination path
     * @return whether the destination belongs to a subscription for sending messages between Lilo and the learner
     */
    private Boolean isSessionTopic(String destination) {
        return (destination != null
                && destination.startsWith("/topic/session/")
                && destination.split("/").length == 4);
    }
}
