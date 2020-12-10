/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Basic Spring Configuration that enables DeepSampler in a Spring Application.
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class DeepSamplerSpringConfig {

    @Bean
    public SpringSamplerInterceptor springSamplerInterceptor() {
        return new SpringSamplerInterceptor();
    }
}
