package org.deepsampler.core.model;

public class SampleExecutionInformation {

    private int timesInvoked = 0;

    public SampleExecutionInformation(int timesInvoked) {
        this.timesInvoked = timesInvoked;
    }

    public void increaseTimesInvoked() {
        timesInvoked++;
    }

    public int getTimesInvoked() {
        return timesInvoked;
    }
}
