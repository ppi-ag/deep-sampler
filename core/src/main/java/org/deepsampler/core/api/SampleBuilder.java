package org.deepsampler.core.api;

import org.deepsampler.core.model.ReturnValueSupplier;
import org.deepsampler.core.model.SampleDefinition;


public class SampleBuilder<T> {

    private final T returningProxy;
    private final SampleDefinition sampleDefinition;

    public SampleBuilder(final T returningProxy, final SampleDefinition sampleDefinition) {
        this.returningProxy = returningProxy;
        this.sampleDefinition = sampleDefinition;
    }

    public SampleBuilder id(final String id) {
        sampleDefinition.setSampleId(id);
        return this;
    }

    public void is(final T sampleReturnValue) {
        sampleDefinition.setReturnValueSupplier(() -> sampleReturnValue);
    }

    public void doing(final ReturnValueSupplier propertySupplier) {
        sampleDefinition.setReturnValueSupplier(propertySupplier);
    }

}
