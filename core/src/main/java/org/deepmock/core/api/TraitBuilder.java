package org.deepmock.core.api;

import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.ReturnValueSupplier;

public class TraitBuilder<T> {

    private final T returningProxy;
    private final Behavior behavior;

    public TraitBuilder(T returningProxy, Behavior behavior) {
        this.returningProxy = returningProxy;
        this.behavior = behavior;
    }

    public void returning(T property) {
        behavior.setReturnValueSupplier(() -> property);
    }

    public void doing(ReturnValueSupplier propertySupplier) {
        behavior.setReturnValueSupplier(propertySupplier);
    }

}
