package org.deepmock.core.internal;

import org.deepmock.core.api.Quantity;

public class FixedQuantity extends Quantity {
    private final int fixedQuantity;

    public FixedQuantity(int i) {
        this.fixedQuantity = i;
    }

    public int getFixedQuantity() {
        return fixedQuantity;
    }
}
