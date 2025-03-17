package com.bdi.agent.profiles;

import com.bdi.agent.service.graph.GraphUpdatingService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockGraphUpdatingService")
@Configuration
public class MockGraphUpdatingService {

    /**
     * Mocks the GraphUpdatingService.
     *
     * @return A mocked GraphUpdatingService.
     */
    @Bean
    @Primary
    public GraphUpdatingService getMockGraphUpdatingService() {
        return Mockito.mock(GraphUpdatingService.class);
    }

}
