/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SampleExecutionInformation {

    private int timesInvoked ;
    private final List<MethodCall> methodCallList = new ArrayList<>();

    public SampleExecutionInformation(final int timesInvoked) {
        this.timesInvoked = timesInvoked;
    }

    public List<MethodCall> getMethodCalls() {
        return Collections.unmodifiableList(methodCallList);
    }

    public void addMethodCall(final MethodCall call) {
        this.methodCallList.add(call);
    }

    public void increaseTimesInvoked() {
        timesInvoked++;
    }

    public int getTimesInvoked() {
        return timesInvoked;
    }
}