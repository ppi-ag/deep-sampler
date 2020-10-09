package org.deepsampler.persistence.json.model;

import java.util.Map;

public interface PersistentModel {
    String getId();
    Map<PersistentSampleMethod, PersistentActualSample> getSampleMethodToSampleMap();
}
