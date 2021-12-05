/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json.model;

import de.ppi.deepsampler.persistence.model.PersistentParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonPersistentParameter implements PersistentParameter {

    private List<Object> args;

    public JsonPersistentParameter() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentParameter(final List<Object> args) {
        setParameter(args);
    }

    @Override
    public List<Object> getParameter() {
        return args;
    }

    public void setParameter(final List<Object> args) {
        this.args = new ArrayList<>(args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonPersistentParameter that = (JsonPersistentParameter) o;
        return Objects.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(args);
    }
}
