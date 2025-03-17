package com.bdi.agent.profiles;

import com.bdi.agent.authorization.JwtTokenUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockJwtTokenUtils")
@Configuration
public class MockJwtTokenUtils {

    /**
     * Mocks the JwtTokenUtils class.
     *
     * @return A mocked JwtTokenUtils.
     */
    @Bean
    @Primary
    public JwtTokenUtils getMockJwtTokenUtils() {
        return Mockito.mock(JwtTokenUtils.class);
    }
}
