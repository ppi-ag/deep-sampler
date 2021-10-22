/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

/**
 * This is a service that cannot be intercepted by guice- or spring-aop and therefore yields errors if it
 * is not excluded from the aop-provider. This class is meant to test this condition.
 */
public final class FinalTestService {

    public static final String GREETING = "Hello World";

    public String getGreeting() {
        return GREETING;
    }
}
