/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.ppi.deepsampler.persistence.model.PersistentActualSample;
import de.ppi.deepsampler.persistence.model.PersistentMethodCall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonPersistentActualSample implements PersistentActualSample {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private final List<JsonPersistentMethodCall> callMap = new ArrayList<>();

    public JsonPersistentActualSample() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public void addCall(final JsonPersistentParameter jsonPersistentParameter, final Object jsonPersistentReturnValue) {
        callMap.add(new JsonPersistentMethodCall(jsonPersistentParameter, jsonPersistentReturnValue));
    }

    @Override
    public List<PersistentMethodCall> getAllCalls() {
        return Collections.unmodifiableList(callMap);
    }

}
