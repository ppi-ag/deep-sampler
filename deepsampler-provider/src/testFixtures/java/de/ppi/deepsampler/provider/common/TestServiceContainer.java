/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;


/**
 * A simple Service that is used as a container for another injected Service, in order to test sampling in deeper object trees.
 */
public abstract class TestServiceContainer {

    public static final String SUFFIX_FROM_SERVICE_CONTAINER = "A Suffix that is added by the container to the return value coming from a delegated service.";

    public abstract TestService getTestService();

    /**
     * Delegates a call to {@link TestService#echoParameter(String)} and adds a value to the value coming from the
     * called method. This is intended to be used to test if delegated objects in deep object trees are intercepted and sampled correctly.
     * @return
     */
    public String augmentValueFromTestService() {
        final String valueFromTestService = getTestService().echoParameter(SamplerAspectTest.VALUE_C);
        return valueFromTestService + SUFFIX_FROM_SERVICE_CONTAINER;
    }

    /**
     * This method returns a non primitive parameter if the method is not mocked.
     * This Method is intended to be used in positive and negative tests.
     *
     * @param someObject The parameter that will be returned unchanged
     * @return the unchanged parameter value
     */
    public TestBean echoParameter(final TestBean someObject) {
        return someObject;
    }

}
