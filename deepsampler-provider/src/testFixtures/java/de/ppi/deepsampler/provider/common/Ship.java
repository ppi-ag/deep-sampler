/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

public enum Ship {
    ENTERPRISE("NCC-1701-D"), VOYAGER("NCC-74656"), DEFIANT("NCC-75633");

    private final String registration;

    Ship(String registration) {
        this.registration = registration;
    }

    public String getRegistration() {
        return registration;
    }
}
