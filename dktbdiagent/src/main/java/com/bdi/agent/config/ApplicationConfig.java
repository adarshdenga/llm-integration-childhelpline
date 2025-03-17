package com.bdi.agent.config;

import com.bdi.agent.utils.FloatComparer;
import com.bdi.agent.utils.ObjectLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Value("${comparerEpsilon}")
    private float comparerEpsilon;

    /**
     * Creates a new FloatComparer.
     *
     * @return the FloatComparer.
     */
    @Bean
    public FloatComparer getFloatComparer() {
        return new FloatComparer(comparerEpsilon);
    }

    /**
     * Creates a new ObjectLock.
     *
     * @return the ObjectLock.
     */
    @Bean
    public ObjectLock objectLock() {
        return new ObjectLock();
    }
}
