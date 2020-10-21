package org.deepsampler.core.internal;

import org.deepsampler.core.api.Quantity;

public class FixedQuantity implements Quantity {

    public static final FixedQuantity NEVER = new FixedQuantity(0);
    public static final FixedQuantity ONCE = new FixedQuantity(1);
    public static final FixedQuantity TWICE = new FixedQuantity(2);

    private final int times;

    public FixedQuantity(final int i) {
        this.times = i;
    }

    @Override
    public int getTimes() {
        return times;
    }

}
