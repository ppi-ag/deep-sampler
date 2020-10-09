package org.deepsampler.persistence.json.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JsonSampleModel implements PersistentModel {

    private String id;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private Map<JsonPersistentSampleMethod, JsonPersistentActualSample> joinPointBehaviorMap = new HashMap<>();

    public JsonSampleModel() {
        //DEFAULT CONS FOR JSON SER/DER
    }

    public JsonSampleModel(String id, Map<JsonPersistentSampleMethod, JsonPersistentActualSample> joinPointBehaviorMap) {
        this.id = id;
        this.joinPointBehaviorMap = new HashMap<>(joinPointBehaviorMap);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<PersistentSampleMethod, PersistentActualSample> getSampleMethodToSampleMap() {
        return Collections.unmodifiableMap(joinPointBehaviorMap);
    }
}
