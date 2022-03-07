/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import de.ppi.deepsampler.core.api.SampleReturnProcessor;

import java.util.*;

public class ExecutionRepository {
    private final Map<Class<?>, ExecutionInformation> executionInformation = new HashMap<>();
    private final List<SampleReturnProcessor> globalProcessors = new ArrayList<>();
    private final Map<SampleDefinition, List<SampleReturnProcessor>> sampleDefinitionSampleReturnProcessorMap = new HashMap<>();

    private static Scope<ExecutionRepository> myInstance = new ThreadScope<>();

    /**
     * Singleton Constructor.
     */
    private ExecutionRepository() {}

    public static synchronized ExecutionRepository getInstance() {
        return myInstance.getOrCreate(ExecutionRepository::new);
    }

    public Map<Class<?>, ExecutionInformation> getAll() {
        return Collections.unmodifiableMap(executionInformation);
    }

    /**
     * Sets the scope of the {@link SampleRepository} end defines the visibility limits of Samples.
     * The default {@link Scope} is {@link ThreadScope}, so by default Samples are not shared across {@link Thread}s.
     *
     * @param executionRepositoryRepository The {@link Scope} that should be used by the {@link SampleRepository}.
     */
    public static synchronized void setScope(Scope<ExecutionRepository> executionRepositoryRepository) {
        Objects.requireNonNull(executionRepositoryRepository, "The ExecutionRepositoryRepository must not be null.");

        ExecutionRepository.myInstance.close();
        ExecutionRepository.myInstance = executionRepositoryRepository;
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
        var sampleReturnProcessors = this.sampleDefinitionSampleReturnProcessorMap.computeIfAbsent(sampleDefinition, k -> new ArrayList<>());
        sampleReturnProcessors.add(sampleReturnProcessor);
    }

    public List<SampleReturnProcessor> getSampleReturnProcessorsFor(SampleDefinition sampleDefinition) {
        return sampleDefinitionSampleReturnProcessorMap.computeIfAbsent(sampleDefinition, k -> new ArrayList<>());
    }

    public void clear() {
        executionInformation.clear();
        globalProcessors.clear();
        sampleDefinitionSampleReturnProcessorMap.clear();
    }
}
