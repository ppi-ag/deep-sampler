/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json.model;

import de.ppi.deepsampler.persistence.model.PersistentParameter;

import java.util.ArrayList;
import java.util.List;

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
}
