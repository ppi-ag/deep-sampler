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
