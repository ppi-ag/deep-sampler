package org.deepsampler.persistence.json.model;

public class JsonPersistentMethodCall implements PersistentMethodCall {
    private JsonPersistentParameter parameter;
    private JsonPersistentReturnValue returnValue;

    public JsonPersistentMethodCall() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentMethodCall(JsonPersistentParameter jsonPersistentParameter, JsonPersistentReturnValue jsonPersistentReturnValue) {
        this.parameter = jsonPersistentParameter;
        this.returnValue = jsonPersistentReturnValue;
    }

    @Override
    public JsonPersistentParameter getPersistentParameter() {
        return parameter;
    }

    @Override
    public JsonPersistentReturnValue getPersistentReturnValue() {
        return returnValue;
    }

    public void setReturnValue(JsonPersistentReturnValue returnValue) {
        this.returnValue = returnValue;
    }

    public void setParameter(JsonPersistentParameter parameter) {
        this.parameter = parameter;
    }
}
