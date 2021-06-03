package de.ppi.deepsampler.provider.common;

public class GenericTestBean<T> {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
