package org.deepsampler.persistence.json.model;

import java.util.Objects;

public class JsonPersistentSampleMethod implements PersistentSampleMethod {
    private String sampleMethodId;

    public JsonPersistentSampleMethod() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentSampleMethod(String sampleMethodId) {
        this.sampleMethodId = sampleMethodId;
    }

    @Override
    public String getSampleMethodId() {
        return sampleMethodId;
    }

    @Override
    public String toString() {
        return sampleMethodId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonPersistentSampleMethod that = (JsonPersistentSampleMethod) o;
        return Objects.equals(sampleMethodId, that.sampleMethodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sampleMethodId);
    }
}
