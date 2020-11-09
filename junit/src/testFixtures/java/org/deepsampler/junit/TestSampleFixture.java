package org.deepsampler.junit;

import org.deepsampler.core.api.Sample;

public class TestSampleFixture implements SamplerFixture {

    @PrepareSampler
    private TestBean testBeanSampler;

    @Override
    public void defineSamplers() {
        Sample.of(testBeanSampler.getSomeInt());
    }
}
