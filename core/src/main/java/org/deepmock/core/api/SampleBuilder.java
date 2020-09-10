package org.deepmock.core.api;

import org.deepmock.core.model.SampleDefinition;
import org.deepmock.core.model.ReturnValueSupplier;

public class SampleBuilder<T> {

    private final T returningProxy;
    private final SampleDefinition sampleDefinition;

    public SampleBuilder(T returningProxy, SampleDefinition sampleDefinition) {
        this.returningProxy = returningProxy;
        this.sampleDefinition = sampleDefinition;
    }

    public void is(T property) {
        sampleDefinition.setReturnValueSupplier(() -> property);
    }

    public void doing(ReturnValueSupplier propertySupplier) {
        sampleDefinition.setReturnValueSupplier(propertySupplier);
    }

}
