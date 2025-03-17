package com.bdi.agent.profiles;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.servlet.http.HttpServletResponse;

@Profile("mockHttpServletResponse")
@Configuration
public class MockHttpServletResponse {

    /**
     * Mocks the HttpServletResponse class.
     *
     * @return A mocked HttpServletResponse.
     */
    @Bean
    @Primary
    public HttpServletResponse getMockHttpServletResponse() {
        return Mockito.mock(HttpServletResponse.class);
    }
}
