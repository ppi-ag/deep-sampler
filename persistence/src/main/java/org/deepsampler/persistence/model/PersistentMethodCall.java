package org.deepsampler.persistence.model;

public interface PersistentMethodCall {
    PersistentParameter getPersistentParameter();
    Object getPersistentReturnValue();
}
