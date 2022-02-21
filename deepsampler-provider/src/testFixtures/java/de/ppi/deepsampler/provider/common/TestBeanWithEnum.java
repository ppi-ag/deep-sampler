/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

public class TestBeanWithEnum {

    private final Ship ship;

    public TestBeanWithEnum(Ship ship) {
        this.ship = ship;
    }

    public Ship getShip() {
        return ship;
    }
}
