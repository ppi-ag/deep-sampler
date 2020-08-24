package org.deepmock.provider.common;

/**
 * A Service that will be instrumented to test the interceptors.
 */
public class TestService {

    /**
     * This method returns a primitve parameter if the method is not mocked.
     * This Method is intended to be used in positive tests.
     *
     * @param param The parameter that will be returned unchanged
     * @return the unchanged parameter value
     */
    public String shouldChangeItsBehavior(String param) {
        return param;
    }

    /**
     * This method returns a primitive parameter if the method is not mocked.
     * This Method is intended to be used in negative tests.
     *
     * @param param The parameter that will be returned unchanged
     * @return the unchanged parameter value
     */
    public String shouldNotChangeItsBehavior(String param) {
        return param;
    }

    /**
     * A method that will always return -1 if it is not mocked
     * @return always -1
     */
    public int shouldChangeItsBehavior() {
        return -1;
    }

    /**
     * This method returns a non primitve parameter if the method is not mocked.
     * This Method is intended to be used in positive tests.
     *
     * @param someObject The parameter that will be returned unchanged
     * @return the unchanged parameter value
     */
    public TestBean shouldChangeItsBehavior(TestBean someObject) {
        return someObject;
    }
}
