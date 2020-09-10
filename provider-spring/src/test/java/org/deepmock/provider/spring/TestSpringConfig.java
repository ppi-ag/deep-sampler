package org.deepmock.provider.spring;

import org.deepmock.provider.common.TestService;
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
    public SpringyfiedTestServiceContainer testServiceContainer(TestService testService) {
        return new SpringyfiedTestServiceContainer(testService);
    }

}
