package org.deepsampler.persistence.json;

import org.deepsampler.core.model.ExecutionInformation;
import org.deepsampler.persistence.json.model.PersistentModel;

import java.util.Map;

public interface SourceManager {
    void record(Map<Class<?>, ExecutionInformation> executionInformation, PersistentSamplerContext samplerContext);
    PersistentModel load(PersistentSamplerContext samplerContext);
}
