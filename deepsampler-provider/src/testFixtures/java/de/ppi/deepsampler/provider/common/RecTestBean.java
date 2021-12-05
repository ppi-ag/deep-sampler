/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

import java.util.Objects;

public class RecTestBean {
    private final RecTestBean testBean;
    private final String value;
    private final Character character;

    public RecTestBean(RecTestBean testBean, String value, Character character) {
        this.testBean = testBean;
        this.value = value;
        this.character = character;
    }

    public RecTestBean getTestBean() {
        return testBean;
    }

    public String getValue() {
        return value;
    }

    public Character getCharacter() {
        return character;
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
