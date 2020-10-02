package org.deepmock.persistence.model;


import java.util.List;

public interface PersistentActualBehavior {
    List<PersistentMethodCall> getAllCalls();
}
