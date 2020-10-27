package org.deepsampler.provider.common;

import java.sql.Date;

/**
 * A Service that will be instrumented to test the interceptors.
 */
public class TestService {

    /**
     * This method returns a primitve parameter if the method is not sampled.
     * This Method is intended to be used in positive and negative tests.
     *
     * @param param The parameter that will be returned unchanged
     * @return the unchanged parameter value
     */
    public String echoParameter(final String param) {
        return param;
    }


    /**
     * A method that will always return -1 if it is not sampled
     * @return always -1
     */
    public int getMinusOne() {
        return -1;
    }

    /**
     * This method returns a non primitive parameter if the method is not mocked.
     * This Method is intended to be used in positive and negative tests.
     *
     * @param someObject The parameter that will be returned unchanged
     * @return the unchanged parameter value
     */
    public TestBean echoParameter(final TestBean someObject) {
        return someObject;
    }

    /**
     * This method is needed to test whether calls of void methods can be verified or not.
     *
     * @param someInt
     */
    public void noReturnValue(final int someInt) {
        // There is nothing to do here, we are only interested in the method call itself.
    };

    public Date testSqlDate(final RecTestBean someObject) {
        return new Date(1);
    }
}
