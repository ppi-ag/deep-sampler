/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

import java.util.Objects;

/**
 * A very simple Bean that represents non primitive types in TestCases.
 */
public class TestBean {

    private String value;

    public TestBean() {
    }

    public TestBean(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestBean testBean = (TestBean) o;
        return Objects.equals(value, testBean.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
