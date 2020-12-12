/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring;

import de.ppi.deepsampler.provider.common.FinalTestService;
import de.ppi.deepsampler.provider.common.TestService;
import de.ppi.deepsampler.provider.testservices.DecoupledTestService;
import de.ppi.deepsampler.provider.testservices.DecoupledTestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class TestSpringConfig {

    @Bean
    public TestService testObjectService() {
        return new TestService();
    }

    @Bean
    public FinalTestService finalTestObjectService() {
        return new FinalTestService();
    }


    @Bean
    @Autowired
    public SpringyfiedTestServiceContainer testServiceContainer(final TestService testService) {
        return new SpringyfiedTestServiceContainer(testService);
    }

    @Bean
    public DecoupledTestService decoupledTestService() {
        return new DecoupledTestServiceImpl();
    }

    @Bean
    public CustomSpringSamplerAspect customSpringSamplerAspect() {
        return new CustomSpringSamplerAspect();
    }

}
