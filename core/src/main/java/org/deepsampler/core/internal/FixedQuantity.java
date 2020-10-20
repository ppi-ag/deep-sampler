package org.deepsampler.core.internal;

import org.deepsampler.core.api.Quantity;

public class FixedQuantity extends Quantity {

    public static final FixedQuantity NEVER = new FixedQuantity(0);
    public static final FixedQuantity ONCE = new FixedQuantity(1);
    public static final FixedQuantity TWICE = new FixedQuantity(2);

    private final int fixedQuantity;

    public FixedQuantity(final int i) {
        this.fixedQuantity = i;
    }

    @Override
    public int getTimes() {
        return fixedQuantity;
    }

}
