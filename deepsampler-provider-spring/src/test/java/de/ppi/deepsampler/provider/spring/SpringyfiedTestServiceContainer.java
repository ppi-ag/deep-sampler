package de.ppi.deepsampler.provider.spring;

import de.ppi.deepsampler.provider.common.TestService;
import de.ppi.deepsampler.provider.common.TestServiceContainer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A simple Bean that serves as a container for another autowired Bean, in order to test sampling in deeper object trees
 */
public class SpringyfiedTestServiceContainer extends TestServiceContainer {


    private final TestService testService;

    @Autowired
    public SpringyfiedTestServiceContainer(final TestService testService) {
        this.testService = testService;
    }


    @Override
    public TestService getTestService() {
        return testService;
    }

}
