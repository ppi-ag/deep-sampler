/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.guice;

import com.google.inject.Guice;
import de.ppi.deepsampler.provider.common.*;
import javax.inject.Inject;


public class GuicePersistentSamplerAspectTest extends PersistentSamplerAspectTest {

    @Inject
    private TestService testService;


    public GuicePersistentSamplerAspectTest() {
        Guice.createInjector(new TestModule()).injectMembers(this);
    }

    @Override
    public TestService getTestService() {
        return testService;
    }

}