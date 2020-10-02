package org.deepmock.persistence.json.model;

import org.deepmock.persistence.model.PersistentActualBehavior;
import org.deepmock.persistence.model.PersistentMethodCall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonPersistentActualBehavior implements PersistentActualBehavior {
    private final List<JsonPersistentMethodCall> callMap = new ArrayList<>();

    public JsonPersistentActualBehavior() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public void addCall(JsonPersistentParameter jsonPersistentParameter, JsonPersistentReturnValue jsonPersistentReturnValue) {
        callMap.add(new JsonPersistentMethodCall(jsonPersistentParameter, jsonPersistentReturnValue));
    }

    @Override
    public List<PersistentMethodCall> getAllCalls() {
        return Collections.unmodifiableList(callMap);
    }

}
