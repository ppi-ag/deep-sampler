package org.deepsampler.persistence.model;

import java.util.Map;

public interface PersistentModel {
    String getId();
    Map<PersistentSampleMethod, PersistentActualSample> getJoinPointBehaviorMap();
}
