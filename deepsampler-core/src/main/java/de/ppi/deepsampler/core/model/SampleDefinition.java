/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SampleDefinition {
    private final SampledMethod sampledMethod;

    private List<Object> parameterValues = new ArrayList<>();
    private List<ParameterMatcher<?>> parameterMatchers = new ArrayList<>();
    private Answer<Throwable> answer;
    private String sampleId;
    private boolean isMarkedForPersistence;

    public SampleDefinition(final SampledMethod sampledMethod) {
        this.sampledMethod = sampledMethod;
        this.sampleId = buildSampleId(sampledMethod);
    }

    private String buildSampleId(final SampledMethod sampledMethod) {
        return sampledMethod.getMethod().toGenericString();
    }

    public SampledMethod getSampledMethod() {
        return sampledMethod;
    }

    public void setParameterMatchers(final List<ParameterMatcher<?>> parameterMatchers) {
        this.parameterMatchers = parameterMatchers;
    }

    public List<ParameterMatcher<?>> getParameterMatchers() {
        return parameterMatchers;
    }

    public int getNumberOfParameters() {
        return parameterMatchers.size();
    }

    @SuppressWarnings("unchecked")
    public <T> ParameterMatcher<T> getParameterMatcherAs(final int i, final Class<T> cls) {
        Objects.requireNonNull(cls);

        return (ParameterMatcher<T>) parameterMatchers.get(i);
    }

    public void setSampleId(final String sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setAnswer(final Answer<Throwable> answer) {
        this.answer = answer;
    }

    public Answer<Throwable> getAnswer() {
        return answer;
    }


    public void setParameterValues(final List<Object> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public List<Object> getParameterValues() {
        return this.parameterValues;
    }


    public boolean isMarkedForPersistence() {
        return isMarkedForPersistence;
    }

    public void setMarkedForPersistence(boolean markedForPersistence) {
        isMarkedForPersistence = markedForPersistence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SampleDefinition that = (SampleDefinition) o;
        //Only these three fields should be compared to identify a SampleDefinition.
        //A method should only be sampled once and is identified by signature independent from its return value!
        //Comparing also 'returnValueSupplier' leads to inconsistent behavior during
        //definition of two SampleDefinitions with same SampleMethod (and args) but different returnValues.
        return Objects.equals(sampledMethod.getMethod(), that.sampledMethod.getMethod()) &&
                Objects.equals(parameterValues, that.parameterValues) &&
                Objects.equals(sampleId, that.sampleId);
    }



    @Override
    public int hashCode() {
        return Objects.hash(sampledMethod.getMethod(), parameterValues, sampleId);
    }

    @Override
    public String toString() {
        return "SampleDefinition{" +
                "sampledMethod=" + sampledMethod +
                ", parameterValues=" + parameterValues +
                ", parameterMatchers=" + parameterMatchers +
                ", answer=" + answer +
                ", sampleId='" + sampleId + '\'' +
                ", isPersistent=" + isMarkedForPersistence +
                '}';
    }
}
