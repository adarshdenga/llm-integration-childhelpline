package com.bdi.agent.profiles;

import com.bdi.agent.service.BeliefService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockBeliefService")
@Configuration
public class MockBeliefService {

    /**
     * Mocks the BeliefService.
     *
     * @return A mocked BeliefService.
     */
    @Bean
    @Primary
    public BeliefService getMockBeliefService() {
        return Mockito.mock(BeliefService.class);
    }
}
