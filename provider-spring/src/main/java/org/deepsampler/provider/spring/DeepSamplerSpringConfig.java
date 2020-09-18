package org.deepsampler.provider.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Basic Spring Configuration that enables DeepSampler in a Spring Application.
 */
@Configuration
@EnableAspectJAutoProxy
public class DeepSamplerSpringConfig {

    @Bean
    public SpringSamplerInterceptor springSamplerInterceptor() {
        return new SpringSamplerInterceptor();
    }
}
