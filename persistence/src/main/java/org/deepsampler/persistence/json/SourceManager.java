package org.deepsampler.persistence.json;

import org.deepsampler.persistence.json.model.PersistentModel;
import org.deepsampler.core.model.ExecutionInformation;

import java.util.Map;

public interface SourceManager {
    void record(Map<Class<?>, ExecutionInformation> executionInformation);
    PersistentModel load();
}
