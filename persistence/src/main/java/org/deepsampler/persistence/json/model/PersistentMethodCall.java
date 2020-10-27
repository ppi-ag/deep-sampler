package org.deepsampler.persistence.json.model;

public interface PersistentMethodCall {
    PersistentParameter getPersistentParameter();
    Object getPersistentReturnValue();
}
