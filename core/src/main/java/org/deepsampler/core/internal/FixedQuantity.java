package org.deepsampler.core.internal;

import org.deepsampler.core.api.Quantity;

public class FixedQuantity extends Quantity {
    private final int fixedQuantity;

    public FixedQuantity(int i) {
        this.fixedQuantity = i;
    }

    public int getTimes() {
        return fixedQuantity;
    }
}
