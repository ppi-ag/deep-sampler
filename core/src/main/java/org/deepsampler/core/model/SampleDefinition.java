package org.deepsampler.core.model;

import java.util.ArrayList;
import java.util.List;

public class SampleDefinition {

    private final SampledMethod sampledMethod;

    private List<ParameterMatcher> parameter = new ArrayList<>();

    private ReturnValueSupplier returnValueSupplier;

    public SampleDefinition(SampledMethod sampledMethod) {
        this.sampledMethod = sampledMethod;
    }

    public SampledMethod getSampledMethod() {
        return sampledMethod;
    }

    public List<ParameterMatcher> getParameter() {
        return parameter;
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
