package org.deepmock.persistence.model;

public interface PersistentMethodCall {
    PersistentParameter getPersistentParameter();
    PersistentReturnValue getPersistentReturnValue();
}
