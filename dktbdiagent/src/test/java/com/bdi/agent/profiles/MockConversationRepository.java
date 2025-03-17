package com.bdi.agent.profiles;

import com.bdi.agent.repository.ConversationRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockConversationRepository")
@Configuration
public class MockConversationRepository {

    /**
     * Mocks the ConversationRepository.
     *
     * @return A mocked ConversationRepository.
     */
    @Bean
    @Primary
    public ConversationRepository getMockConversationRepository() {
        return Mockito.mock(ConversationRepository.class);
    }
}
