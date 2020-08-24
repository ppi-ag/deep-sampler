package org.deepmock.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BehaviorExecutionInformation {

    private int timesInvoked = 0;
    private List<MethodCall> methodCallList = new ArrayList<>();

    public BehaviorExecutionInformation(int timesInvoked) {
        this.timesInvoked = timesInvoked;
    }

    public List<MethodCall> getMethodCalls() {
        return Collections.unmodifiableList(methodCallList);
    }

    public void addMethodCall(MethodCall call) {
        this.methodCallList.add(call);
    }

    public void increaseTimesInvoked() {
        timesInvoked++;
    }

    public int getTimesInvoked() {
        return timesInvoked;
    }
}
