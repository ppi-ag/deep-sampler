/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

public class GenericBeagle<T> extends Dog {

    private T food;

    public GenericBeagle(String name) {
        super(name);
    }

    public T getFood() {
        return food;
    }

    public void setFood(T food) {
        this.food = food;
    }
}
