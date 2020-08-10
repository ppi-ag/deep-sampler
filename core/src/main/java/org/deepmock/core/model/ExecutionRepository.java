package org.deepmock.core.model;

import java.util.HashMap;
import java.util.Map;

public class ExecutionRepository {
    private static final ThreadLocal<Map<Class<?>, ExecutionInformation>> executionInformation = ThreadLocal.withInitial(() -> new HashMap<>());

    public ExecutionInformation getExecutionInformation(Class<?> cls) {
        return executionInformation.get().get(cls);
    }
}
