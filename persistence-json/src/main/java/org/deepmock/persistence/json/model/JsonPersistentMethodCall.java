package org.deepmock.persistence.json.model;

public class JsonPersistentMethodCall {
    private JsonPersistentParameter parameter;
    private JsonPersistentReturnValue returnValue;

    public JsonPersistentMethodCall() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentMethodCall(JsonPersistentParameter jsonPersistentParameter, JsonPersistentReturnValue jsonPersistentReturnValue) {
        this.parameter = jsonPersistentParameter;
        this.returnValue = jsonPersistentReturnValue;
    }

    public JsonPersistentParameter getJsonPersistentParameter() {
        return parameter;
    }

    public JsonPersistentReturnValue getJsonPersistentReturnValue() {
        return returnValue;
    }

    public void setReturnValue(JsonPersistentReturnValue returnValue) {
        this.returnValue = returnValue;
    }

    public void setParameter(JsonPersistentParameter parameter) {
        this.parameter = parameter;
    }
}
