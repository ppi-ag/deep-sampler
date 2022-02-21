/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

/**
 * Represents a quantity to measure the number of times a stubbed method got called.
 */
public interface Quantity {

    /**
     * @return number of times this quantity represents
     */
    int getTimes();
}
