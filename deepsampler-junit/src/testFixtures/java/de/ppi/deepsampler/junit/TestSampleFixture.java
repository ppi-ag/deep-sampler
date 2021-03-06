/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.api.Sample;

@SuppressWarnings("unused")
public class TestSampleFixture implements SamplerFixture {

    @PrepareSampler
    private TestBean testBeanSampler;

    @Override
    public void defineSamplers() {
        Sample.of(testBeanSampler.getSomeInt());
    }
}
