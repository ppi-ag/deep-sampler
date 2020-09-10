package org.deepmock.provider.spring;

import org.deepmock.provider.common.TestService;
import org.deepmock.provider.common.TestServiceContainer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A simple Bean that serves as a container for another autowired Bean, in order to test sampling in deeper object trees
 */
public class SpringyfiedTestServiceContainer extends TestServiceContainer {


    private TestService testService;

    @Autowired
    public SpringyfiedTestServiceContainer(TestService testService) {
        this.testService = testService;
    }


    @Override
    public TestService getTestService() {
        return testService;
    }


}
