package org.deepmock.persistence.json.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JsonPersonalityModel {

    private String id;
    private Map<JsonPersistentJoinPoint, JsonPersistentActualBehavior> joinPointBehaviorMap = new HashMap<>();

    public JsonPersonalityModel() {
        //DEFAULT CONS FOR JSON SER/DER
    }

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
