package com.bdi.agent.profiles;

import com.bdi.agent.service.ConstraintService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockConstraintService")
@Configuration
public class MockConstraintService {

    /**
     * Mocks the ConstraintService.
     *
     * @return A mocked ConstraintService.
     */
    @Bean
    @Primary
    public ConstraintService getMockConstraintService() {
        return Mockito.mock(ConstraintService.class);
    }
}