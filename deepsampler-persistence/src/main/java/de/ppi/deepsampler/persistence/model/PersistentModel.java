package de.ppi.deepsampler.persistence.model;

import java.util.Map;

public interface PersistentModel {
    String getId();
    Map<PersistentSampleMethod, PersistentActualSample> getSampleMethodToSampleMap();
}
