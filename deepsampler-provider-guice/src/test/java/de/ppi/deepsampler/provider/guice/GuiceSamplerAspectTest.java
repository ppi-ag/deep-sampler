/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.guice;

import com.google.inject.Guice;
import de.ppi.deepsampler.provider.common.FinalTestService;
import de.ppi.deepsampler.provider.common.SamplerAspectTest;
import de.ppi.deepsampler.provider.common.TestService;
import de.ppi.deepsampler.provider.common.TestServiceContainer;
import de.ppi.deepsampler.provider.testservices.DecoupledTestService;

import javax.inject.Inject;


public class GuiceSamplerAspectTest extends SamplerAspectTest {

    public static final String VALUE_FROM_OUTER_CLASS = " additional stuff to ensure that this method has not been changed";

    @Inject
    private GuicyfiedTestServiceContainer testServiceContainer;

    @Inject
    private TestService testService;

    @Inject
    private FinalTestService finalTestService;

    @Inject
    private DecoupledTestService decoupledTestService;


    public GuiceSamplerAspectTest() {
        Guice.createInjector(new TestModule()).injectMembers(this);
    }

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