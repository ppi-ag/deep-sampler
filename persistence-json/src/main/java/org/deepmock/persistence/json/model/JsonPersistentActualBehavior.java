package org.deepmock.persistence.json.model;

import java.util.*;

public class JsonPersistentActualBehavior {
    private final List<JsonPersistentMethodCall> callMap = new ArrayList<>();

    public JsonPersistentActualBehavior() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public void addCall(JsonPersistentParameter jsonPersistentParameter, JsonPersistentReturnValue jsonPersistentReturnValue) {
        callMap.add(new JsonPersistentMethodCall(jsonPersistentParameter, jsonPersistentReturnValue));
    }

    public List<JsonPersistentMethodCall> getAllCalls() {
        return Collections.unmodifiableList(callMap);
    }

}
