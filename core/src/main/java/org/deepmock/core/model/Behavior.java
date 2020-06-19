package org.deepmock.core.model;

import java.util.List;
import java.util.function.Supplier;

public class Behavior {

    private JoinPoint joinPoint;

    private List<ParameterMatcher> parameter;

    private Supplier<Object> returnValueSupplier;

    public Behavior(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public JoinPoint getJoinPoint() {
        return joinPoint;
    }

    public void setJoinPoint(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public List<ParameterMatcher> getParameter() {
        return parameter;
    }

    public void setParameter(List<ParameterMatcher> parameter) {
        this.parameter = parameter;
    }

    public Supplier<Object> getReturnValueSupplier() {
        return returnValueSupplier;
    }

    public void setReturnValueSupplier(Supplier<Object> returnValueSupplier) {
        this.returnValueSupplier = returnValueSupplier;
    }
}
