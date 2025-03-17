package com.bdi.agent.profiles;

import com.bdi.agent.service.AgentService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockAgentService")
@Configuration
public class MockAgentService {

        /**
         * Mocks the AgentService.
         *
         * @return A mocked AgentService.
         */
        @Bean
        @Primary
        public AgentService getMockAgentService() {
            return Mockito.mock(AgentService.class);
        }
}
