package org.deepsampler.core.model;

import org.deepsampler.core.error.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;

public class SampleRepository {

    private ThreadLocal<List<SampleDefinition>> samples = ThreadLocal.withInitial(() -> new ArrayList<>());
    private ThreadLocal<SampleDefinition> currentSample = new ThreadLocal<>();

    private static SampleRepository myInstance;

    public static synchronized SampleRepository getInstance() {
        if (myInstance == null) {
            myInstance = new SampleRepository();
        }

        return myInstance;
    }

    public void add(SampleDefinition sampleDefinition) {
        if (sampleDefinition.getSampledMethod() == null) {
            throw new InvalidConfigException("%s must define a %s", sampleDefinition.toString(), SampledMethod.class.getSimpleName());
        }
        setCurrentSample(sampleDefinition);
        samples.get().add(sampleDefinition);
    }


    public SampleDefinition find(SampledMethod wantedSampledMethod, Object... args) {
        for (SampleDefinition sampleDefinition : samples.get()) {
            SampledMethod sampledMethod = sampleDefinition.getSampledMethod();
            boolean classMatches = sampledMethod.getTarget().isAssignableFrom(wantedSampledMethod.getTarget());
            boolean methodMatches = sampledMethod.getMethod().equals(wantedSampledMethod.getMethod());
            boolean argumentsMatches = argumentsMatch(sampleDefinition, args);

            if (classMatches && methodMatches && argumentsMatches) {
                return sampleDefinition;
            }
        }

        return null;
    }

    private boolean argumentsMatch(SampleDefinition sampleDefinition, Object[] arguments) {
        List<ParameterMatcher> parameterMatchers = sampleDefinition.getParameter();

        if (parameterMatchers.size() != arguments.length) {
            return false;
        }

        for (int i = 0; i < arguments.length; i++) {
            if (!parameterMatchers.get(0).matches(arguments[i])) {
                return false;
            }
        }

        return true;
    }

    private void setCurrentSample(SampleDefinition sampleDefinition) {
        currentSample.set(sampleDefinition);
    }

    public SampleDefinition getCurrentSampleDefinition() {
        return currentSample.get();
    }

    public List<SampleDefinition> getSamples() {
        return samples.get();
    }

    public void clear() {
        samples.get().clear();
    }

    public boolean isEmpty() {
        return samples.get() == null || samples.get().isEmpty();
    }
}
