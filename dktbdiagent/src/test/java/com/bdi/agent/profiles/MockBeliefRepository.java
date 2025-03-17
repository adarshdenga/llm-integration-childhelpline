package com.bdi.agent.profiles;

import com.bdi.agent.repository.BeliefRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockBeliefRepository")
@Configuration
public class MockBeliefRepository {

    /**
     * Mocks the BeliefRepository.
     *
     * @return A mocked BeliefRepository.
     */
    @Bean
    @Primary
    public BeliefRepository getMockBeliefRepository() {
        return Mockito.mock(BeliefRepository.class);
    }
}
