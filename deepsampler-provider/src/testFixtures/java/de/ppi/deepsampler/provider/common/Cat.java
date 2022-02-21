/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

public class Cat implements Animal{

    private String name;

    public Cat(String name) {
        this.name =name;
    }

    @Override
    public String getName() {
        return name;
    }

}
