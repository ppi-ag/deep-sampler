package org.deepsampler.persistence.json.model;

import org.deepsampler.persistence.bean.Bean;
import org.deepsampler.persistence.model.PersistentParameter;

import java.util.List;

public class JsonPersistentParameter implements PersistentParameter {
    private List<Bean> args;

    public JsonPersistentParameter() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentParameter(List<Bean> args) {
        this.args = args;
    }

    @Override
    public List<Bean> getParameter() {
        return args;
    }

    public void setParameter(List<Bean> args) {
        this.args = args;
    }
}
