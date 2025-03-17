package com.bdi.agent.profiles;

import com.bdi.agent.service.graph.OptimalPathService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockOptimalPathService")
@Configuration
public class MockOptimalPathService {

    /**
     * Mocks the OptimalPathService.
     *
     * @return A mocked OptimalPathService.
     */
    @Bean
    @Primary
    public OptimalPathService getMockOptimalPathService() {
        return Mockito.mock(OptimalPathService.class);
    }

}