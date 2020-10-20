package org.deepsampler.persistence.json.model;

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
