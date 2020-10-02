package org.deepmock.persistence.json.model;

import org.deepmock.persistence.model.PersistentJoinPoint;

import java.util.Objects;

public class JsonPersistentJoinPoint implements PersistentJoinPoint {
    private String joinPointId;

    public JsonPersistentJoinPoint() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentJoinPoint(String joinPointId) {
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
        JsonPersistentJoinPoint that = (JsonPersistentJoinPoint) o;
        return Objects.equals(joinPointId, that.joinPointId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(joinPointId);
    }
}
