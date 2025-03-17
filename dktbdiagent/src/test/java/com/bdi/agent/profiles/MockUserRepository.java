package com.bdi.agent.profiles;

import com.bdi.agent.repository.UserRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockUserRepository")
@Configuration
public class MockUserRepository {

    /**
     * Mocks the UserRepository.
     *
     * @return A mocked UserRepository.
     */
    @Bean
    @Primary
    public UserRepository getMockUserRepository() {
        return Mockito.mock(UserRepository.class);
    }

}
