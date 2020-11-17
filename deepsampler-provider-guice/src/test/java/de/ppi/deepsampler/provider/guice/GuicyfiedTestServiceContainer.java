/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.guice;

import de.ppi.deepsampler.provider.common.TestService;
import de.ppi.deepsampler.provider.common.TestServiceContainer;

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
