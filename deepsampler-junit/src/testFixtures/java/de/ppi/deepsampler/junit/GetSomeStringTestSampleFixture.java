/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.api.PersistentSample;

@SuppressWarnings("unused")
public class GetSomeStringTestSampleFixture implements SamplerFixture {

    @PrepareSampler
    private TestBean testBeanSampler;

    @Override
    public void defineSamplers() {
        PersistentSample.of(testBeanSampler.getSomeString());
    }
}
