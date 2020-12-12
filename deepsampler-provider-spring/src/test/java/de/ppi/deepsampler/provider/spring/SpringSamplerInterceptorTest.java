/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring;

import de.ppi.deepsampler.provider.common.FinalTestService;
import de.ppi.deepsampler.provider.common.SamplerInterceptorTest;
import de.ppi.deepsampler.provider.common.TestService;
import de.ppi.deepsampler.provider.common.TestServiceContainer;
import de.ppi.deepsampler.provider.testservices.DecoupledTestService;
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

    @Autowired
    private FinalTestService finalTestService;

    @Autowired
    private DecoupledTestService decoupledTestService;


    @Override
    public TestService getTestService() {
        return testService;
    }

    @Override
    public FinalTestService getFinalTestService() {
        return finalTestService;
    }

    @Override
    public TestServiceContainer getTestServiceContainer() {
        return testServiceContainer;
    }

    @Override
    public DecoupledTestService getDecoupledTestService() {
        return decoupledTestService;
    }

}