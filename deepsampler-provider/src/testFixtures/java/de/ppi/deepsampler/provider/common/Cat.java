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
