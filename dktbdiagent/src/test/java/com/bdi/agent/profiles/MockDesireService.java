package com.bdi.agent.profiles;

import com.bdi.agent.service.DesireService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockDesireService")
@Configuration
public class MockDesireService {

    /**
     * Mocks the DesireService.
     *
     * @return A mocked DesireService.
     */
    @Bean
    @Primary
    public DesireService getMockDesireService() {
        return Mockito.mock(DesireService.class);
    }
}
