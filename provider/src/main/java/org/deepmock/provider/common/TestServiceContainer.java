package org.deepmock.provider.common;


/**
 * A simple Service that is used as a container for another injected Service, in order to test mocking in deeper object trees.
 */
public abstract class TestServiceContainer {

    public static final String SUFFIX_FROM_SERVICE_CONTAINER = "A Suffix that is added by the container to the return value coming from a delegated service.";

    public abstract TestService getTestService();

    /**
     * Delegates a call to {@link TestService}
     * @return
     */
    public String doSomeThingWithTestObjectService() {
        String valueFromTestService = getTestService().shouldChangeItsBehavior(BehaviorInterceptorTest.VALUE_C);
        return valueFromTestService + SUFFIX_FROM_SERVICE_CONTAINER;
    }

}
