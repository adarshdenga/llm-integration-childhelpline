package com.bdi.agent.profiles;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.servlet.FilterChain;

@Profile("mockFilterChain")
@Configuration
public class MockFilterChain {

    /**
     * Mocks Spring's built-in FilterChain class.
     *
     * @return A mocked FilterChain.
     */
    @Bean
    @Primary
    public FilterChain getMockFilterChain() {
        return Mockito.mock(FilterChain.class);
    }
}
