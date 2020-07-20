package org.deepmock.persistence.json;

import java.util.Collections;
import java.util.List;

public class JsonPersonalityModel {

    private String id;
    private List<JsonPersistentBehavior> behaviorList;

    public JsonPersonalityModel(String id, List<JsonPersistentBehavior> behaviorList) {
        this.id = id;
        this.behaviorList = behaviorList;
    }

    public String getId() {
        return id;
    }

    public List<JsonPersistentBehavior> getBehaviorList() {
        return Collections.unmodifiableList(behaviorList);
    }
}
