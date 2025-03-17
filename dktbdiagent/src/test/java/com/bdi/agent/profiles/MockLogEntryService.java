package com.bdi.agent.profiles;

import com.bdi.agent.service.LogEntryService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockLogEntryService")
@Configuration
public class MockLogEntryService {

    /**
     * Mocks the LogEntryService.
     *
     * @return A mocked LogEntryService.
     */
    @Bean
    @Primary
    public LogEntryService getMockLogEntryService() {
        return Mockito.mock(LogEntryService.class);
    }
}
