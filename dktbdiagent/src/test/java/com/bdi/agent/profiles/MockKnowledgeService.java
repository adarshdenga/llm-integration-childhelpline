package com.bdi.agent.profiles;

import com.bdi.agent.service.KnowledgeService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockKnowledgeService")
@Configuration
public class MockKnowledgeService {

    /**
     * Mocks the KnowledgeService.
     *
     * @return A mocked KnowledgeService.
     */
    @Bean
    @Primary
    public KnowledgeService getMockKnowledgeService() {
        return Mockito.mock(KnowledgeService.class);
    }
}
