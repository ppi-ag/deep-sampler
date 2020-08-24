package org.deepmock.persistence.json.model;

public class JsonPersistentReturnValue {
    private Object returnValue;

    public JsonPersistentReturnValue() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
}
