package org.deepsampler.core.model;

public class SampleExecutionInformation {

    private int timesInvoked = 0;
    private List<MethodCall> methodCallList = new ArrayList<>();

    public SampleExecutionInformation(int timesInvoked) {
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
