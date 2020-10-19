package org.deepsampler.core.api;

import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.ReturnValueSupplier;


public class SampleBuilder<T> {

    private final T returningProxy;
    private final SampleDefinition sampleDefinition;

    public SampleBuilder(T returningProxy, SampleDefinition sampleDefinition) {
        this.returningProxy = returningProxy;
        this.sampleDefinition = sampleDefinition;
    }

    public SampleBuilder id(String id) {
        sampleDefinition.setSampleId(id);
        return this;
    }

    public void is(T sampleReturnValue) {
        sampleDefinition.setReturnValueSupplier(() -> sampleReturnValue);
    }

    public void doing(ReturnValueSupplier propertySupplier) {
        sampleDefinition.setReturnValueSupplier(propertySupplier);
    }

}
