package org.deepmock.core.model;

import java.util.ArrayList;
import java.util.List;

public class Behavior {

    private JoinPoint joinPoint;
    private List<ParameterMatcher> parameter = new ArrayList<>();
    private ReturnValueSupplier returnValueSupplier;
    private String behaviorId;

    public Behavior(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public void setJoinPoint(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public JoinPoint getJoinPoint() {
        return joinPoint;
    }

    public List<ParameterMatcher> getParameter() {
        return parameter;
    }

    public void setBehaviorId(String behaviorId) {
        this.behaviorId = behaviorId;
    }

    public String getBehaviorId() {
        return behaviorId;
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
