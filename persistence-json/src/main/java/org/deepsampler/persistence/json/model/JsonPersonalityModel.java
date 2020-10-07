package org.deepsampler.persistence.json.model;

import org.deepsampler.persistence.model.PersistentActualSample;
import org.deepsampler.persistence.model.PersistentJoinPoint;
import org.deepsampler.persistence.model.PersistentModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JsonPersonalityModel implements PersistentModel {

    private String id;
    private Map<JsonPersistentSampleMethod, JsonPersistentActualSample> joinPointBehaviorMap = new HashMap<>();

    public JsonPersonalityModel() {
        //DEFAULT CONS FOR JSON SER/DER
    }

    public JsonPersonalityModel(String id, Map<JsonPersistentSampleMethod, JsonPersistentActualSample> joinPointBehaviorMap) {
        this.id = id;
        this.joinPointBehaviorMap = new HashMap<>(joinPointBehaviorMap);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<PersistentJoinPoint, PersistentActualSample> getJoinPointBehaviorMap() {
        return Collections.unmodifiableMap(joinPointBehaviorMap);
    }
}
