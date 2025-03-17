package com.bdi.agent.profiles;

import com.bdi.agent.utils.ConstraintProvider;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockConstraintProvider")
@Configuration
public class MockConstraintProvider {

    /**
     * Mocks the ConstraintProvider.
     *
     * @return A mocked ConstraintProvider.
     */
    @Bean
    @Primary
    public ConstraintProvider getMockConstraintProvider() {
        return Mockito.mock(ConstraintProvider.class);
    }

}