package org.deepsampler.core.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class SampleDefinition {
    private final SampledMethod sampledMethod;

    /**
     * The Parametervalues for the {@link SampledMethod}.
     */
    private List<Object> parameterValues = new ArrayList<>();

    private List<ParameterMatcher> parameter = new ArrayList<>();
    private ReturnValueSupplier returnValueSupplier;
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

    public List<ParameterMatcher> getParameter() {
        return parameter;
    }

    public void setSampleId(final String sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setParameter(final List<ParameterMatcher> parameter) {
        this.parameter = parameter;
    }

    public ReturnValueSupplier getReturnValueSupplier() {
        return returnValueSupplier;
    }

    public void setReturnValueSupplier(final ReturnValueSupplier returnValueSupplier) {
        this.returnValueSupplier = returnValueSupplier;
    }

    public List<Object> getParameterValues() {
        return this.parameterValues;
    }

    public void setParameterValues(final List<Object> paramterValues) {
        this.parameterValues = paramterValues;
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
}
