package de.ppi.deepsampler.persistence.model;

import java.util.List;

public class PersistentList implements Persistable {
    private List<Object> persistableList;

    public PersistentList() {
        // DEFAULT CONST FOR JSON DESERIALIZATION
    }

    public PersistentList(List<Object> persistableList) {
        this.persistableList = persistableList;
    }

    public List<Object> getPersistableList() {
        return persistableList;
    }

    public void setPersistableList(List<Object> persistableList) {
        this.persistableList = persistableList;
    }
}
