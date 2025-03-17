package com.bdi.agent.profiles;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;

@Profile("mockUserDetailsService")
@Configuration
public class MockUserDetailsService {

    /**
     * Mocks the UserDetailsService.
     *
     * @return A mocked UserDetailsService.
     */
    @Bean
    @Primary
    public UserDetailsService getMockUserDetailsService() {
        return Mockito.mock(UserDetailsService.class);
    }
}
