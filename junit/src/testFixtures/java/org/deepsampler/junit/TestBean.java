package org.deepsampler.junit;

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

    @SuppressWarnings("unused")
    public String getSomeString() {
        return someString;
    }

    @SuppressWarnings("unused")
    public void setSomeString(final String someString) {
        this.someString = someString;
    }

    public int getSomeInt() {
        return someInt;
    }

    @SuppressWarnings("unused")
    public void setSomeInt(final int someInt) {
        this.someInt = someInt;
    }
}
