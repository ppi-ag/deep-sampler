package org.deepmock.persistence.model;

import java.util.Map;

public interface PersistentModel {
    String getId();
    Map<PersistentJoinPoint, PersistentActualBehavior> getJoinPointBehaviorMap();
}
