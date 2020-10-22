package org.deepsampler.persistence.json.model;

public class JsonPersistentReturnValue implements PersistentReturnValue {
    private Object returnValue;

    public JsonPersistentReturnValue() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentReturnValue(final Object returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(final Object returnValue) {
        this.returnValue = returnValue;
    }
}
