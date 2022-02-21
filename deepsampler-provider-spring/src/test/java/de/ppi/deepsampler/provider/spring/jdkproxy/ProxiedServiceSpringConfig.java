/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring.jdkproxy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.lang.reflect.Proxy;

/**
 * A Spring {@link Configuration} that creates a bean that is already a jdk proxy, instead of a vanilla object.
 */
@Configuration
@EnableAspectJAutoProxy
public class ProxiedServiceSpringConfig {

    /**
     * This bean is not a vanilla object, but a jdk proxy.
     * @return a jdk proxy of the interface {@link ProxiedTestService}
     */
    @Bean
    public ProxiedTestService proxiedTestService() {
        return (ProxiedTestService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ProxiedTestService.class},
                (proxy, method, args) -> args[0]);
    }

    @Bean
    public ProxiedServiceSpringSamplerAspect customSpringSamplerAspect() {
        return new ProxiedServiceSpringSamplerAspect();
    }

}
