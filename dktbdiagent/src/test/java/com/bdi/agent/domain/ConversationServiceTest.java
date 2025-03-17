package com.bdi.agent.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Conversation;
import com.bdi.agent.model.Role;
import com.bdi.agent.model.User;
import com.bdi.agent.repository.ConversationRepository;
import com.bdi.agent.service.ConversationService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"mockConversationRepository"})
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ConversationServiceTest {

    @Autowired
    private transient ConversationRepository mockConversationRepository;

    @Autowired
    private transient ConversationService conversationService;

    @Test
    public void renameConversationTest() {
        Agent agent = new Agent(1L, "testId", "test", null, null,
                null, 0L, "", true,
                0L, 0.0f, null, false, null);
        User user = new User("j_doe", "1VeryUnsafePassword!", "j.doe@mail.com", Role.LEARNER);
        Conversation conversation = new Conversation("Old", LocalDateTime.now(), agent, user);
        when(mockConversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        conversationService.renameConversation(1L, "New");
        verify(mockConversationRepository).save(conversation);
        assertEquals(conversation.getConversationName(), "New");
    }

    @Test
    public void deleteConversationTest() {
        Agent agent = new Agent(1L, "testId", "test", null,
                null, null, 0L, "",
                true, 0L, 0.0f, null, false, null);
        User user = new User("j_doe", "1VeryUnsafePassword!", "j.doe@mail.com", Role.LEARNER);
        Conversation conversation = new Conversation("Name", LocalDateTime.now(), agent, user);
        when(mockConversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        conversationService.deleteConversation(1L);
        verify(mockConversationRepository).delete(conversation);
    }

}
