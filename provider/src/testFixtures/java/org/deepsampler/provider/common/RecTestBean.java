package org.deepsampler.provider.common;

import java.util.Objects;

public class RecTestBean {
    private final RecTestBean testBean;
    private final String value;

    public RecTestBean(RecTestBean testBean, String value) {
        this.testBean = testBean;
        this.value = value;
    }

    public RecTestBean getTestBean() {
        return testBean;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecTestBean that = (RecTestBean) o;
        return Objects.equals(testBean, that.testBean) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testBean, value);
    }
}
