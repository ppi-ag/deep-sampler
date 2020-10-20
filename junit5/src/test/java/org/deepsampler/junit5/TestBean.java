package org.deepsampler.junit5;

/**
 * A simple Bean that represents non primitive types in TestCases.
 */
public class TestBean {

    private String someString;
    private int someInt;

    public TestBean(final String someString, final int someInt) {
        this.someString = someString;
        this.someInt = someInt;
    }

    public String getSomeString() {
        return someString;
    }

    public void setSomeString(final String someString) {
        this.someString = someString;
    }

    public int getSomeInt() {
        return someInt;
    }

    public void setSomeInt(final int someInt) {
        this.someInt = someInt;
    }
}
