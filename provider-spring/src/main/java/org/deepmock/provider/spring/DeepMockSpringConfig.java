package org.deepmock.provider.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Basic Spring Configuration that enables DeepMock in a Spring-Application.
 */
@Configuration
@EnableAspectJAutoProxy
public class DeepMockSpringConfig {

    @Bean
    public SpringBehaviorInterceptor springBehaviorInterceptor() {
        return new SpringBehaviorInterceptor();
    }
}
