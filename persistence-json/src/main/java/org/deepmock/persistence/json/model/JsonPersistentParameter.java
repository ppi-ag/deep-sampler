package org.deepmock.persistence.json.model;

import org.deepmock.persistence.bean.Bean;
import org.deepmock.persistence.model.PersistentParameter;

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
