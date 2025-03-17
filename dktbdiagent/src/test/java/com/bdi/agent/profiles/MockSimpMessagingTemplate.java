package com.bdi.agent.profiles;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Profile("mockSimpMessagingTemplate")
@Configuration
public class MockSimpMessagingTemplate {

    /**
     * Mocks the SimpMessagingTemplate. This is used so that the websockets are not used in tests where they are
     * not required / not being tested.
     *
     * @return A mocked SimpMessagingTemplate.
     */
    @Bean
    @Primary
    public SimpMessagingTemplate getSimpMessagingTemplate() {
        return Mockito.mock(SimpMessagingTemplate.class);
    }
}
