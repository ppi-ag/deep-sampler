/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.StubMethodInvocation;

/**
 * Hook-in processor for influencing the stubbing process.
 *
 * @author Rico Schrage
 */
@FunctionalInterface
public interface SampleReturnProcessor {
    /**
     * Will run after a stubbed method has been called.
     *
     * @param sampleDefinition the sampleDefinition which is responsible for the stubbing of the method call
     * @param stubMethodInvocation the actual method call
     * @param returnValue the real return value of the method call
     *
     * @return the new return value of the method call
     */
    Object onReturn(final SampleDefinition sampleDefinition, final StubMethodInvocation stubMethodInvocation, final Object returnValue);
}
