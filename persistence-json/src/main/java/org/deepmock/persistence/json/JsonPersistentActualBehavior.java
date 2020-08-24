package org.deepmock.persistence.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonPersistentActualBehavior {
    private final Map<JsonPersistentParameter, JsonPersistentReturnValue> callMap = new HashMap<>();

    public JsonPersistentActualBehavior() {
        // nothing to set
    }

    public Map<JsonPersistentParameter, JsonPersistentReturnValue> getCallMap() {
        return Collections.unmodifiableMap(callMap);
    }

    public JsonPersistentReturnValue getReturnValue(JsonPersistentParameter param) {
        return callMap.get(param);
    }
}
