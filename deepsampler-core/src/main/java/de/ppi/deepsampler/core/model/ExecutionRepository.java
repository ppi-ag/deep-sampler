package de.ppi.deepsampler.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExecutionRepository {
    private final ThreadLocal<Map<Class<?>, ExecutionInformation>> executionInformation = ThreadLocal.withInitial(HashMap::new);

    private static ExecutionRepository myInstance;

    /**
     * Singleton Constructor.
     */
    private ExecutionRepository() {}

    public static synchronized ExecutionRepository getInstance() {
        if (myInstance == null) {
            myInstance = new ExecutionRepository();
        }

        return myInstance;
    }

    public Map<Class<?>, ExecutionInformation> getAll() {
        return Collections.unmodifiableMap(executionInformation.get());
    }

    public ExecutionInformation getOrCreate(final Class<?> cls) {
        return executionInformation.get().computeIfAbsent(cls, k -> new ExecutionInformation());
    }

    public void clear() {
        executionInformation.get().clear();
        executionInformation.remove();
    }
}
