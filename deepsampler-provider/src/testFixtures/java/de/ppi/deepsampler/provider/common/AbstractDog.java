/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

public abstract class AbstractDog implements Animal {


    public static class InternalDog extends AbstractDog {

        private String name;

        public InternalDog(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
