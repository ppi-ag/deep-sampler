/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

public class HunterCat extends Cat {

    private Mouse food;

    public HunterCat(String name) {
        super("Tom");
    }

    public Mouse getFood() {
        return food;
    }

    public void setFood(Mouse food) {
        this.food = food;
    }
}
