package com.bdi.agent.api;

import com.bdi.agent.authorization.JwtTokenUtils;
import com.bdi.agent.exceptions.InvalidJoinRequest;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Conversation;
import com.bdi.agent.model.User;
import com.bdi.agent.model.api.OptimalPathModel;
import com.bdi.agent.repository.AgentRepository;
import com.bdi.agent.service.UserService;
import com.bdi.agent.service.graph.OptimalPathService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("optimal-path")
public class OptimalPathController {

    private final OptimalPathService optimalPathService;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserService userService;
    private final AgentRepository agentRepository;

    /**
     * Creates a OptimalPathController.
     *
     * @param optimalPathService The service for the optimal path functionalities.
     * @param jwtTokenUtils utilities class for JWT tokens.
     * @param userService The service for users.
     * @param agentRepository agent repository.
     */
    @Autowired
    public OptimalPathController(OptimalPathService optimalPathService,
                                 JwtTokenUtils jwtTokenUtils,
                                 UserService userService, AgentRepository agentRepository) {
        this.optimalPathService = optimalPathService;
        this.jwtTokenUtils = jwtTokenUtils;
        this.userService = userService;
        this.agentRepository = agentRepository;
    }

    /**
     * Gets the optimal path from the current state of the conversation following the Five Phase Model.
     *
     * @param conversationId The id of the conversation.
     * @return 200 OK and the OptimalPathRequest if possible, and otherwise a bad request.
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getOptimalPath(@PathVariable("conversationId") String conversationId,
                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            String trainerName = jwtTokenUtils.retrieveUsernameFromToken(authHeader);

            // get conversations from the given session ID
            List<Conversation> conversations = agentRepository.findAll()
                    .stream()
                    // gets the agent with the given session id
                    .filter(agent -> agent.getUserId().equals(conversationId))
                    .map(Agent::getConversation)
                    .toList();

            if (conversations.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST).body("There are no sessions with the given id.");
            }

            List<User> learners = new ArrayList<>();
            for (Conversation conversation : conversations) {
                // add all users that correspond to the given session
                // AND have the given trainer assigned as their trainer
                learners.addAll(userService
                        .getUsersFromConversationId(conversation.getConversationId())
                        .stream().filter(user -> {
                            try {
                                return userService.isAssignedTrainer(user.getUsername(), trainerName)
                                        || user.getUsername().equals(trainerName);
                            } catch (InvalidJoinRequest e) {
                                e.printStackTrace();
                            }
                            return false;
                        })
                        .toList());
            }

            // no users correspond to the given session
            if (learners.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST).body("You are not assigned as trainer.");
            }

            OptimalPathModel optimalPathRequest = optimalPathService.generateOptimalPathRequest(conversationId);
            return new ResponseEntity<>(optimalPathRequest, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
