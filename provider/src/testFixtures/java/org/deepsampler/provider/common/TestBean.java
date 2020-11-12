package org.deepsampler.provider.common;

import java.util.Objects;

/**
 * A very simple Bean that represents non primitive types in TestCases.
 */
public class TestBean {

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TestBean that = (TestBean) o;
        return Objects.equals(this, that);
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }
}
