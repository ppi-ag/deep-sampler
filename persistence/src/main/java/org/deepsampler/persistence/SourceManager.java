package org.deepsampler.persistence;

import org.deepsampler.persistence.model.PersistentModel;
import org.deepsampler.core.model.ExecutionInformation;

import java.util.Map;

public interface SourceManager {
    void record(Map<Class<?>, ExecutionInformation> executionInformation);
    PersistentModel load();
}
