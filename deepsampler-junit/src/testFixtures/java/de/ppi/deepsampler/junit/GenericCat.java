/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

public class GenericCat<T> {

    private T prey;

    public T getPrey() {
        return prey;
    }

    public void setPrey(T prey) {
        this.prey = prey;
    }
}
