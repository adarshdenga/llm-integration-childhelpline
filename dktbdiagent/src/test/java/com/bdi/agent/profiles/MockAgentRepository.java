package com.bdi.agent.profiles;

import com.bdi.agent.repository.AgentRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockAgentRepository")
@Configuration
public class MockAgentRepository {

    /**
     * Mocks the AgentRepository.
     *
     * @return A mocked AgentRepository.
     */
    @Bean
    @Primary
    public AgentRepository getMockAgentRepository() {
        return Mockito.mock(AgentRepository.class);
    }

}
