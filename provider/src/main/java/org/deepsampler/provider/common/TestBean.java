package org.deepsampler.provider.common;

/**
 * A simple Bean that represents non primitive types in TestCases.
 */
public class TestBean {

    private String someString;
    private int someInt;

    public TestBean(String someString, int someInt) {
        this.someString = someString;
        this.someInt = someInt;
    }

    public String getSomeString() {
        return someString;
    }

    public void setSomeString(String someString) {
        this.someString = someString;
    }

    public int getSomeInt() {
        return someInt;
    }

    public void setSomeInt(int someInt) {
        this.someInt = someInt;
    }
}
