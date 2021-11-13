/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import java.time.Instant;

public class TestService {

    public static final String CATS_DEFAULT_NAME = "Spot";

    private String catsName = CATS_DEFAULT_NAME;

    private Instant defaultInstant = Instant.ofEpochMilli(0);

    public Instant getInstant() {
        return defaultInstant;
    }

    public Cat getCat() {
        return new Cat(catsName);
    }

    public void setCatsName(String catsName) {
        this.catsName = catsName;
    }

    public void setDefaultInstant(Instant defaultInstant) {
        this.defaultInstant = defaultInstant;
    }
}
