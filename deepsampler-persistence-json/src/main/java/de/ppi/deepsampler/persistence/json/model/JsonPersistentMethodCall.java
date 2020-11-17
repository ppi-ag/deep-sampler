package de.ppi.deepsampler.persistence.json.model;

import de.ppi.deepsampler.persistence.model.PersistentMethodCall;

public class JsonPersistentMethodCall implements PersistentMethodCall {
    private JsonPersistentParameter parameter;
    private Object returnValue;

    public JsonPersistentMethodCall() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentMethodCall(final JsonPersistentParameter jsonPersistentParameter, final Object jsonPersistentReturnValue) {
        this.parameter = jsonPersistentParameter;
        this.returnValue = jsonPersistentReturnValue;
    }

    @Override
    public JsonPersistentParameter getPersistentParameter() {
        return parameter;
    }

    @Override
    public Object getPersistentReturnValue() {
        return returnValue;
    }

    public void setReturnValue(final Object returnValue) {
        this.returnValue = returnValue;
    }

    public void setParameter(final JsonPersistentParameter parameter) {
        this.parameter = parameter;
    }
}
