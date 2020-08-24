package org.deepmock.persistence;

import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.ExecutionInformation;

import java.util.List;
import java.util.Map;

public interface SourceManager {
    void record(Map<Class<?>, ExecutionInformation> executionInformation);
    List<Behavior> load();
}
