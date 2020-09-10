package org.deepmock.provider.standalone;

import com.google.inject.Guice;
import org.deepmock.provider.common.SamplerInterceptorTest;
import org.deepmock.provider.common.TestService;
import org.deepmock.provider.common.TestServiceContainer;

import javax.inject.Inject;


public class GuiceSamplerInterceptorTest extends SamplerInterceptorTest {

    public static final String VALUE_FROM_OUTER_CLASS = " additional stuff to ensure that this method has not been changed";

    @Inject
    private GuicyfiedTestServiceContainer testServiceContainer;

    @Inject
    private TestService testService;


    public GuiceSamplerInterceptorTest() {
        Guice.createInjector(new DeepSamplerModule()).injectMembers(this);
    }

    @Override
    public TestService getTestService() {
        return testService;
    }

    @Override
    public TestServiceContainer getTestServiceContainer() {
        return testServiceContainer;
    }


}