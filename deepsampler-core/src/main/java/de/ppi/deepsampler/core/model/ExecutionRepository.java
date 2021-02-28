/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import de.ppi.deepsampler.core.api.SampleReturnProcessor;

import java.util.*;

public class ExecutionRepository {
    private final Map<Class<?>, ExecutionInformation> executionInformation = new HashMap<>();
    private final List<SampleReturnProcessor> globalProcessors = new ArrayList<>();
    private final Map<SampleDefinition, List<SampleReturnProcessor>> sampleDefinitionSampleReturnProcessorMap = new HashMap<>();

    private static final ThreadLocal<ExecutionRepository> myInstance = ThreadLocal.withInitial(ExecutionRepository::new);

    /**
     * Singleton Constructor.
     */
    private ExecutionRepository() {}

    public static synchronized ExecutionRepository getInstance() {
        return myInstance.get();
    }

    public Map<Class<?>, ExecutionInformation> getAll() {
        return Collections.unmodifiableMap(executionInformation);
    }

    public ExecutionInformation getOrCreate(final Class<?> cls) {
        return executionInformation.computeIfAbsent(cls, k -> new ExecutionInformation());
    }

    public void addGlobalSampleReturnProcessor(SampleReturnProcessor sampleReturnProcessor) {
        this.globalProcessors.add(sampleReturnProcessor);
    }

    public List<SampleReturnProcessor> getGlobalProcessors() {
        return Collections.unmodifiableList(globalProcessors);
    }

    public void addSampleReturnProcessor(SampleDefinition sampleDefinition, SampleReturnProcessor sampleReturnProcessor) {
        List<SampleReturnProcessor> sampleReturnProcessors = this.sampleDefinitionSampleReturnProcessorMap.computeIfAbsent(sampleDefinition, k -> new ArrayList<>());
        sampleReturnProcessors.add(sampleReturnProcessor);
    }

    public List<SampleReturnProcessor> getSampleReturnProcessorsFor(SampleDefinition sampleDefinition) {
        return sampleDefinitionSampleReturnProcessorMap.computeIfAbsent(sampleDefinition, k -> new ArrayList<>());
    }

    public void clear() {
        executionInformation.clear();
        globalProcessors.clear();
        sampleDefinitionSampleReturnProcessorMap.clear();
        myInstance.remove();
    }
}
