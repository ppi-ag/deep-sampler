package org.deepmock.persistence.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonPersonalityModel {

    private String id;
    private Map<JsonPersistentJoinPoint, JsonPersistentActualBehavior> joinPointBehaviorMap = new HashMap<>();

    public JsonPersonalityModel(String id, Map<JsonPersistentJoinPoint, JsonPersistentActualBehavior> joinPointBehaviorMap) {
        this.id = id;
        this.joinPointBehaviorMap = new HashMap<>(joinPointBehaviorMap);
    }

    public String getId() {
        return id;
    }

    public Map<JsonPersistentJoinPoint, JsonPersistentActualBehavior> getJoinPointBehaviorMap() {
        return Collections.unmodifiableMap(joinPointBehaviorMap);
    }
}
