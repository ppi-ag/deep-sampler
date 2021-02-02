/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SampleDefinition {
    private final SampledMethod sampledMethod;

    private List<Object> parameterValues = new ArrayList<>();
    private List<ParameterMatcher<?>> parameterMatchers = new ArrayList<>();
    private Answer<Exception> answer;
    private String sampleId;

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

    public void setAnswer(final Answer<Exception> answer) {
        this.answer = answer;
    }

    public Answer<Exception> getAnswer() {
        return answer;
    }


    public void setParameterValues(final List<Object> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public List<Object> getParameterValues() {
        return this.parameterValues;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        final SampleDefinition that = (SampleDefinition) other;

        //Only these three fields should be compared to identify a SampleDefinition.
        //A method should only be sampled once and is identified by signature independent from its return value!
        //Comparing also 'returnValueSupplier' leads to inconsistent behavior during
        //definition of two SampleDefinitions with same SampleMethod (and args) but different returnValues.
        return new EqualsBuilder()
                .append(this.sampledMethod.getMethod(), that.sampledMethod.getMethod())
                .append(this.parameterValues, that.parameterValues)
                .append(this.sampleId, that.sampleId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(this.sampledMethod)
                .append(this.parameterValues)
                .append(this.sampleId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "SampleDefinition{" +
                "sampledMethod=" + sampledMethod +
                ", parameterValues=" + parameterValues +
                ", parameterMatchers=" + parameterMatchers +
                ", answer=" + answer +
                ", sampleId='" + sampleId + '\'' +
                '}';
    }
}
