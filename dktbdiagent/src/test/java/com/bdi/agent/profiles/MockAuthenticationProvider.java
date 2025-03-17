package com.bdi.agent.profiles;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;

@Profile("mockAuthenticationProvider")
@Configuration
public class MockAuthenticationProvider {

    /**
     * Mocks the AuthenticationManager.
     *
     * @return A mocked AuthenticationManager.
     */
    @Bean
    @Primary
    public AuthenticationProvider getMockAuthenticationManager() {
        return Mockito.mock(AuthenticationProvider.class);
    }
}
