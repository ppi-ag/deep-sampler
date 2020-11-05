package org.deepsampler.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes the actual call of a stubbed method.
 */
public class StubMethodInvocation {
    private List<Object> parameters = new ArrayList<>();
    private final Object stubInstance;

    public StubMethodInvocation(final List<Object> parameters, final Object stubInstance) {
        this.parameters = parameters;
        this.stubInstance = stubInstance;
    }

    /**
     * If the called method is defined with parameters, the values of these parameters can be accessed using this getter.
     * @return The parameter values of a particular method call. The {@link List} is empty if the method has no parameters.
     */
    public List<Object> getParameters() {
        return parameters;
    }


    /**
     * Gives access to the instance of the stubbed class where the current method is called on.
     * @return The instance of the stub
     */
    public Object getStubInstance() {
        return stubInstance;
    }
}
