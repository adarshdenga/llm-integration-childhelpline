package com.bdi.agent.profiles;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.servlet.http.HttpServletRequest;

@Profile("mockHttpServletRequest")
@Configuration
public class MockHttpServletRequest {

    /**
     * Mocks the HttpServletRequest class.
     *
     * @return A mocked HttpServletRequest.
     */
    @Bean
    @Primary
    public HttpServletRequest getMockHttpServletRequest() {
        return Mockito.mock(HttpServletRequest.class);
    }
}
