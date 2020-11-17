/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring;

import de.ppi.deepsampler.provider.common.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DeepSamplerSpringConfig.class)
public class TestSpringConfig {

    @Bean
    public TestService testObjectService() {
        return new TestService();
    }

    @Bean
    @Autowired
    public SpringyfiedTestServiceContainer testServiceContainer(final TestService testService) {
        return new SpringyfiedTestServiceContainer(testService);
    }

}
