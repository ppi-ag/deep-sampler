/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

import java.util.Arrays;
import java.util.Objects;

/**
 * A very simple Bean that represents non primitive types in TestCases containing a byte[].
 */
public class TestBeanWithBytes {

    private byte[] someBytes;

    public TestBeanWithBytes() {
    }

    public TestBeanWithBytes(byte[] someBytes) {
        this.someBytes = someBytes;
    }

    public byte[] getSomeBytes() {
        return someBytes;
    }

    public void setSomeBytes(byte[] someBytes) {
        this.someBytes = someBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestBeanWithBytes that = (TestBeanWithBytes) o;
        return Arrays.equals(someBytes, that.someBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(someBytes);
    }
}
