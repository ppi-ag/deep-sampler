package org.deepmock.persistence.json.model;

import org.deepmock.persistence.model.PersistentActualBehavior;
import org.deepmock.persistence.model.PersistentJoinPoint;
import org.deepmock.persistence.model.PersistentModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JsonPersonalityModel implements PersistentModel {

    private String id;
    private Map<JsonPersistentJoinPoint, JsonPersistentActualBehavior> joinPointBehaviorMap = new HashMap<>();

    public JsonPersonalityModel() {
        //DEFAULT CONS FOR JSON SER/DER
    }

    public JsonPersonalityModel(String id, Map<JsonPersistentJoinPoint, JsonPersistentActualBehavior> joinPointBehaviorMap) {
        this.id = id;
        this.joinPointBehaviorMap = new HashMap<>(joinPointBehaviorMap);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<PersistentJoinPoint, PersistentActualBehavior> getJoinPointBehaviorMap() {
        return Collections.unmodifiableMap(joinPointBehaviorMap);
    }
}
