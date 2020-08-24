package org.deepmock.persistence.json.model;

import java.util.List;

public class JsonPersistentParameter {
    private List<Object> args;

    public JsonPersistentParameter() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentParameter(List<Object> args) {
        this.args = args;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }
}
