package org.deepsampler.core.api;

import org.deepsampler.core.model.ReturnValueSupplier;
import org.deepsampler.core.model.SampleDefinition;

import java.util.Objects;


public class SampleBuilder<T> {

    private final SampleDefinition sampleDefinition;


    @SuppressWarnings("unused")
    public SampleBuilder(final T returningProxy, final SampleDefinition sampleDefinition) {
        Objects.requireNonNull(sampleDefinition, "the SampleDefinition must not be null.");

        this.sampleDefinition = sampleDefinition;
    }

    public void is(final T sampleReturnValue) {
        sampleDefinition.setReturnValueSupplier(() -> sampleReturnValue);
    }

    public void is(final ReturnValueSupplier propertySupplier) {
        sampleDefinition.setReturnValueSupplier(propertySupplier);
    }

}
