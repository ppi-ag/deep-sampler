/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.internal;

import de.ppi.deepsampler.core.api.Quantity;

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
