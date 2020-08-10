package org.deepmock.core.model;

import java.util.List;

public class Behavior {

    private final JoinPoint joinPoint;

    private List<ParameterMatcher> parameter;

    private ReturnValueSupplier returnValueSupplier;

    public Behavior(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public JoinPoint getJoinPoint() {
        return joinPoint;
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
