package org.deepmock.persistence.json;

import java.util.List;

public class JsonPersistentBehavior {
    private JsonPersistentJoinPoint joinPoint;
    private List<JsonPersistentArgumentMatcher> argumentMatcherList;
    private JsonPersistentReturnValueSupplier returnValueSupplier;
}
