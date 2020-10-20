package org.deepsampler.provider.guice;

import org.deepsampler.provider.common.TestServiceContainer;
import org.deepsampler.provider.common.TestService;

import javax.inject.Inject;

/**
 * A simple Service that serves as a container for another by Guice injected Service. It is used to test sampling in deeper object trees.
 */
public class GuicyfiedTestServiceContainer extends TestServiceContainer {

    @Inject
    private TestService testService;


    @Override
    public TestService getTestService() {
        return testService;
    }
}
