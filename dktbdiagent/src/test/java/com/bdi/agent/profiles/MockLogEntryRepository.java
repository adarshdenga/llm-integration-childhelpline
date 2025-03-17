package com.bdi.agent.profiles;

import com.bdi.agent.repository.LogEntryRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockLogEntryRepository")
@Configuration
public class MockLogEntryRepository {

    /**
     * Mocks the LogEntryRepository.
     *
     * @return A mocked LogEntryRepository.
     */
    @Bean
    @Primary
    public LogEntryRepository getMockLogEntryRepository() {
        return Mockito.mock(LogEntryRepository.class);
    }
}
