package com.bdi.agent.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bdi.agent.authorization.JwtTokenUtils;
import com.bdi.agent.model.Conversation;
import com.bdi.agent.model.User;
import com.bdi.agent.model.api.BeliefChangeClientModel;
import com.bdi.agent.model.api.ChatMetaDataModel;
import com.bdi.agent.model.api.MessageModel;
import com.bdi.agent.service.AgentService;
import com.bdi.agent.service.ConversationService;
import com.bdi.agent.service.ReportService;
import com.bdi.agent.service.UserService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("history")
public class ChatHistoryController {

    private final ConversationService conversationService;
    private final UserService userService;
    private final AgentService agentService;
    private final ReportService reportService;
    private final JwtTokenUtils jwtTokenUtils;

    /**
     * Instantiates a new Chat History Controller.
     *
     * @param conversationService the conversationService for accessing conversations.
     * @param userService the userService for accessing users.
     * @param agentService the agentService for accessing agents.
     * @param jwtTokenUtils utilities class for JWT tokens
     */
    @Autowired
    public ChatHistoryController(ConversationService conversationService,
                                 UserService userService,
                                 AgentService agentService, JwtTokenUtils jwtTokenUtils,
                                 ReportService reportService) {
        this.conversationService = conversationService;
        this.userService = userService;
        this.agentService = agentService;
        this.reportService = reportService;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    /**
     * Fetches the list of chat history entries. Each entry consists of metadata only. This metadata is used on the
     * frontend to display a list of clickable chats. Clicking the chat in the webpage will result in a different
     * endpoint being called for the actual messages.
     *
     * @param authHeader header containing JWT token
     */
    @CrossOrigin("*")
    @GetMapping("/all")
    public ResponseEntity<?> getChatHistoryMetaDataByUser(@RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                      String authHeader) {

        try {
            String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
            User user = userService.getByUsername(username);

            return new ResponseEntity<>(conversationService.getAllConversationsByUserId(user.getId())
                    .stream()
                    .map(chat -> new ChatMetaDataModel(chat.getConversationId(),
                            chat.getConversationName(), chat.getTimestamp()))
                    .toArray(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a conversation. Deleting the conversation will also delete the agent, all beliefs and desires
     * related to that agent, and the report file of the conversation.
     *
     * @param conversationId the ID of the conversation to delete
     * @param authHeader header containing token
     */
    @CrossOrigin("*")
    @DeleteMapping("/{conversationId}/delete")
    public ResponseEntity<String> deleteConversation(@PathVariable("conversationId") Long conversationId,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        if (conversationService.verifyConversationBelongsToUser(conversationId, username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Conversation does not belong to user.");
        }

        Conversation conversation = conversationService.getConversation(conversationId);
        reportService.deleteReport(conversation.getReportFilePath());

        boolean deleted = conversationService.deleteConversation(conversationId);
        if (deleted) {
            return ResponseEntity.ok("Conversation deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Renames a conversation. The name of the conversation does not have to be unique, it only serves as a
     * human-readable informal "identification" on the frontend.
     *
     * @param conversationId the ID of the conversation to rename
     * @param newName the new name
     * @param authHeader header containing token
     */
    @CrossOrigin("*")
    @PutMapping("/{conversationId}/rename")
    public ResponseEntity<?> renameConversation(@PathVariable("conversationId") Long conversationId,
                                                @RequestParam String newName,
                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        if (conversationService.verifyConversationBelongsToUser(conversationId, username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Conversation does not belong to user.");
        }

        Conversation conversation = conversationService.renameConversation(conversationId, newName);

        if (conversation != null) {
            return ResponseEntity.ok(
                    "Renamed Conversation " + conversationId + " to " + conversation.getConversationName()
            );
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Fetches the full conversation, aka the message history, of a conversation.
     *
     * @param conversationId the ID of the conversation to fully fetch the messages of
     * @param authHeader header containing token
     */
    @CrossOrigin("*")
    @GetMapping("/{conversationId}/chat")
    public ResponseEntity<?> getFullConversation(@PathVariable("conversationId") Long conversationId,
                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        if (conversationService.verifyConversationBelongsToUser(conversationId, username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Conversation does not belong to user.");
        }

        Conversation conversation = conversationService.getConversation(conversationId);

        if (conversation != null) {
            List<MessageModel> response = agentService.getConversation(conversation.getAgent().getUserId());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Fetches the full transition history of a conversation.
     *
     * @param conversationId the ID of the conversation to fully fetch the transitions of
     * @param authHeader header containing token
     */
    @CrossOrigin("*")
    @GetMapping("/{conversationId}/transitions")
    public ResponseEntity<?> getTransitionHistory(@PathVariable("conversationId") Long conversationId,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        if (conversationService.verifyConversationBelongsToUser(conversationId, username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Conversation does not belong to user.");
        }

        Conversation conversation = conversationService.getConversation(conversationId);
        if (conversation != null) {
            List<BeliefChangeClientModel> res = agentService.getPastTransitions(conversation.getAgent().getUserId());
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Creates and sends a downloadable report of the conversation. The information that is included in the report is
     * customizable through the RequestParam booleans.
     *
     * @param authHeader header containing token
     * @param conversationId the ID of the conversation to fully fetch the transitions of
     * @param showAbbreviations boolean for showing abbreviations of beliefs and desires (e.g. 'B3', 'D1')
     * @param showBeliefSetTo boolean for showing all belief updates rather than only increases and decreases
     * @param showBeliefUpdateCause boolean for showing the cause behind each belief update
     * @param showDesireUpdate boolean for showing desire updates
     * @param showNewValue boolean for showing the numeric values of beliefs that get updated
     */
    @CrossOrigin("*")
    @GetMapping("/{conversationId}/report")
    public ResponseEntity<byte[]> downloadReport(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable("conversationId") Long conversationId,
            @RequestParam(value = "abbreviations", defaultValue = "false") boolean showAbbreviations,
            @RequestParam(value = "all_belief_updates", defaultValue = "false") boolean showBeliefSetTo,
            @RequestParam(value = "desire_updates", defaultValue = "false") boolean showDesireUpdate,
            @RequestParam(value = "belief_update_causes", defaultValue = "false") boolean showBeliefUpdateCause,
            @RequestParam(value = "belief_values", defaultValue = "false") boolean showNewValue) throws IOException {
        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        if (conversationService.verifyConversationBelongsToUser(conversationId, username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Conversation conversation = conversationService.getConversation(conversationId);

        conversation.setReportFilePath(reportService.createReport(conversation.getAgent(),
                     showAbbreviations, showDesireUpdate, showBeliefSetTo, showBeliefUpdateCause, showNewValue));

        conversationService.saveConversation(conversation);

        String filePath = conversation.getReportFilePath();

        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment",
                conversation.getConversationName().toLowerCase().replaceAll(" ", "_"));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        // Return the file as a ResponseEntity with headers and status code
        return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
    }
}
