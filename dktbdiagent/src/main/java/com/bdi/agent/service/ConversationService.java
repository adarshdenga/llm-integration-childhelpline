package com.bdi.agent.service;

import com.bdi.agent.model.Conversation;
import com.bdi.agent.repository.ConversationRepository;
import com.bdi.agent.repository.UserRepository;
import com.bdi.agent.utils.ObjectLock;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ObjectLock objectLock;

    /**
     * Constructor for the ConversationService.
     *
     * @param userRepository the UserRepository
     * @param conversationRepository the ConversationRepository
     */
    @Autowired
    public ConversationService(UserRepository userRepository, ConversationRepository conversationRepository,
                               ObjectLock objectLock) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.objectLock = objectLock;
    }

    public void saveConversation(Conversation conversation) {
        conversationRepository.save(conversation);
    }

    /**
     * Fetches all conversations belonging to some specific user.
     *
     * @param userId the ID of the user
     */
    public List<Conversation> getAllConversationsByUserId(Long userId) {
        synchronized (objectLock) {
            return conversationRepository.findByUserId(userId);
        }
    }

    /**
     * Fetches all conversations that encapsulate an agent bound to a certain sessionId.
     *
     * @param sessionId the ID of the Rasa session
     * @return list of conversations
     */
    public List<Conversation> getAllConversationsBySessionId(String sessionId) {
        synchronized (objectLock) {
            return conversationRepository.findBySessionId(sessionId);
        }
    }

    /**
     * Deletes all conversations that encapsulate an agent bound to a certain sessionId, and decrements the user's
     * counter that keeps track of how many valid conversations this user has had. This method should delete at most
     * one agent, but this method is more fault-tolerant.
     *
     * @param sessionId the ID of the Rasa session
     */
    public void deleteAllConversationsBySessionId(String sessionId) {
        synchronized (objectLock) {
            conversationRepository.deleteAll(conversationRepository.findBySessionId(sessionId));
        }
    }

    /**
     * Fetches a conversation by its ID.
     *
     * @param conversationId the ID of the conversation
     */
    public Conversation getConversation(Long conversationId) {
        synchronized (objectLock) {
            return conversationRepository.getById(conversationId);
        }
    }

    /**
     * Deletes a conversation. This also deletes the agent that the conversation represents, and the beliefs, actions
     * and desires that are connected to it.
     *
     * @param conversationId the ID of the conversation to delete
     */
    public boolean deleteConversation(Long conversationId) {
        synchronized (objectLock) {
            Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);

            if (optionalConversation.isPresent()) {
                Conversation conversation = optionalConversation.get();
                if (conversation.getUser() != null) {
                    conversation.getUser().incrementNumDeletedConversations();
                }
                conversationRepository.delete(conversation);
                return true;
            }
            return false;
        }
    }

    /**
     * Deletes a conversation. This also deletes the agent that the conversation represents, and the beliefs, actions
     * and desires that are connected to it.
     *
     * @param conversationId the ID of the conversation to rename
     * @param newName the new name for the conversation
     */
    public Conversation renameConversation(Long conversationId, String newName) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);

        if (optionalConversation.isPresent()) {
            Conversation conversation = optionalConversation.get();
            conversation.setConversationName(newName);
            conversationRepository.save(conversation);

            return conversation;
        }
        return null;
    }

    /**
     * Checks whether the conversation belongs to the given user.
     *
     * @param conversationId conversation ID
     * @param username name of the given user
     * @return true if the conversation belongs to the user, false otherwise
     */
    public boolean verifyConversationBelongsToUser(Long conversationId, String username) {
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with name " + username + " does not exist."))
                .getId();

        Optional<Conversation> conversation = getAllConversationsByUserId(userId)
                .stream().filter(c -> c.getConversationId().equals(conversationId)).findAny();

        return conversation.isEmpty();
    }

}
