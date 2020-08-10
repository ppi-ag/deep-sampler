package org.deepmock.core.model;

public class BehaviorExecutionInformation {

    private int timesInvoked = 0;

    public BehaviorExecutionInformation(int timesInvoked) {
        this.timesInvoked = timesInvoked;
    }

    public void increaseTimesInvoked() {
        timesInvoked++;
    }

    public int getTimesInvoked() {
        return timesInvoked;
    }
}
