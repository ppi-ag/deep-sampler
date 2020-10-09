package org.deepsampler.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SampleDefinition {
    private static final AtomicLong ONGOING_NUMBER = new AtomicLong();

    private final SampledMethod sampledMethod;

    private List<ParameterMatcher> parameter = new ArrayList<>();
    private ReturnValueSupplier returnValueSupplier;
    private String sampleId;

    public SampleDefinition(SampledMethod sampledMethod) {
        this.sampledMethod = sampledMethod;
        this.sampleId = buildSampleId(sampledMethod);
    }

    private String buildSampleId(SampledMethod sampledMethod) {
        return sampledMethod.getMethod().toGenericString();
    }

    public SampledMethod getSampledMethod() {
        return sampledMethod;
    }

    public List<ParameterMatcher> getParameter() {
        return parameter;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setParameter(List<ParameterMatcher> parameter) {
        this.parameter = parameter;
    }

    public ReturnValueSupplier getReturnValueSupplier() {
        return returnValueSupplier;
    }

    public void setReturnValueSupplier(ReturnValueSupplier returnValueSupplier) {
        this.returnValueSupplier = returnValueSupplier;
    }
}
