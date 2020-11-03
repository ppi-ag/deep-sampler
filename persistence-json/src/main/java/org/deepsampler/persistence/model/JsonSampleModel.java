package org.deepsampler.persistence.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JsonSampleModel implements PersistentModel {

    private String id;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodToSampleMap = new HashMap<>();

    public JsonSampleModel() {
        //DEFAULT CONS FOR JSON SER/DER
    }

    public JsonSampleModel(final String id, final Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodToSampleMap) {
        this.id = id;
        this.sampleMethodToSampleMap = new HashMap<>(sampleMethodToSampleMap);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<PersistentSampleMethod, PersistentActualSample> getSampleMethodToSampleMap() {
        return Collections.unmodifiableMap(sampleMethodToSampleMap);
    }
}
