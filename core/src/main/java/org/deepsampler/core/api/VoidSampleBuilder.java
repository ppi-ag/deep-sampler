package org.deepsampler.core.api;

import org.deepsampler.core.model.ReturnValueSupplier;
import org.deepsampler.core.model.SampleDefinition;

public class VoidSampleBuilder {

    private final SampleDefinition sampleDefinition;

    public VoidSampleBuilder(SampleDefinition sampleDefinition) {
        this.sampleDefinition = sampleDefinition;
    }

    public void isCalled(Quantity quantity) {

    }

}
