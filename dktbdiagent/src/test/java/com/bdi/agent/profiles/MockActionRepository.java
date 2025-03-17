package com.bdi.agent.profiles;

import com.bdi.agent.repository.ActionRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockActionRepository")
@Configuration
public class MockActionRepository {

    /**
     * Mocks the ActionRepository.
     *
     * @return A mocked ActionRepository.
     */
    @Bean
    @Primary
    public ActionRepository getMockActionRepository() {
        return Mockito.mock(ActionRepository.class);
    }
}
