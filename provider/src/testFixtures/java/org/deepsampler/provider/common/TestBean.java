package org.deepsampler.provider.common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A very simple Bean that represents non primitive types in TestCases.
 */
public class TestBean {

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other, true);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }
}
