package org.deepsampler.persistence.json.model;

import org.deepsampler.persistence.model.PersistentSampleMethod;

import java.util.Objects;

public class JsonPersistentSampleMethod implements PersistentSampleMethod {
    private String joinPointId;

    public JsonPersistentSampleMethod() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentSampleMethod(String joinPointId) {
        this.joinPointId = joinPointId;
    }

    @Override
    public String getJoinPointId() {
        return joinPointId;
    }

    @Override
    public String toString() {
        return joinPointId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonPersistentSampleMethod that = (JsonPersistentSampleMethod) o;
        return Objects.equals(joinPointId, that.joinPointId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(joinPointId);
    }
}
