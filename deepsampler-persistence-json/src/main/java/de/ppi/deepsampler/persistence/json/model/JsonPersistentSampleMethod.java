/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json.model;

import de.ppi.deepsampler.persistence.model.PersistentSampleMethod;

import java.util.Objects;

public class JsonPersistentSampleMethod implements PersistentSampleMethod {
    private String sampleMethodId;

    public JsonPersistentSampleMethod() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentSampleMethod(final String sampleMethodId) {
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JsonPersistentSampleMethod that = (JsonPersistentSampleMethod) o;
        return Objects.equals(sampleMethodId, that.sampleMethodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sampleMethodId);
    }
}
