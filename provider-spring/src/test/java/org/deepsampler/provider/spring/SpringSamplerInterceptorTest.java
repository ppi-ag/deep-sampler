package org.deepsampler.provider.spring;

import org.deepsampler.provider.common.SamplerInterceptorTest;
import org.deepsampler.provider.common.TestService;
import org.deepsampler.provider.common.TestServiceContainer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestSpringConfig.class)
public class SpringSamplerInterceptorTest extends SamplerInterceptorTest {

    public static final String VALUE_FROM_OUTER_CLASS = " additional stuff to ensure that this method has not been changed";

    @Autowired
    private SpringyfiedTestServiceContainer testServiceContainer;

    @Autowired
    private TestService testService;


    @Override
    public TestService getTestService() {
        return testService;
    }

    @Override
    public TestServiceContainer getTestServiceContainer() {
        return testServiceContainer;
    }


}