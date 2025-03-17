package com.bdi.agent.profiles;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Profile("mockPasswordEncoder")
@Configuration
public class MockPasswordEncoder {

    /**
     * Mocks the BCryptPasswordEncoder class.
     *
     * @return A mocked BCryptPasswordEncoder.
     */
    @Bean
    @Primary
    public BCryptPasswordEncoder getMockPasswordEncoder() {
        return Mockito.mock(BCryptPasswordEncoder.class);
    }
}
