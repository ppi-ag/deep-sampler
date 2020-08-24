package org.deepmock.persistence.json.model;

public class JsonPersistentJoinPoint {
    private String joinPointId;

    public JsonPersistentJoinPoint() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentJoinPoint(String joinPointId) {
        this.joinPointId = joinPointId;
    }

    public String getJoinPointId() {
        return joinPointId;
    }

    @Override
    public String toString() {
        return joinPointId;
    }
}
