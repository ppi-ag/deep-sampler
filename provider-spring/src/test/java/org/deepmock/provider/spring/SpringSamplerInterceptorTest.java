package org.deepmock.provider.spring;

import org.deepmock.provider.common.SamplerInterceptorTest;
import org.deepmock.provider.common.TestService;
import org.deepmock.provider.common.TestServiceContainer;
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