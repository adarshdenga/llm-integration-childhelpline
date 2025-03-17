package com.bdi.agent.profiles;

import com.bdi.agent.service.ActionService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockActionService")
@Configuration
public class MockActionService {

    /**
     * Mocks the ActionService.
     *
     * @return A mocked ActionService.
     */
    @Bean
    @Primary
    public ActionService getMockActionService() {
        return Mockito.mock(ActionService.class);
    }
}
