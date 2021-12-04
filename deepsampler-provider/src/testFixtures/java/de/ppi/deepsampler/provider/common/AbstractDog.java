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
