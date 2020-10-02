package org.deepmock.persistence;

import org.deepmock.core.model.ExecutionInformation;
import org.deepmock.persistence.model.PersistentModel;

import java.util.Map;

public interface SourceManager {
    void record(Map<Class<?>, ExecutionInformation> executionInformation);
    PersistentModel load();
}
