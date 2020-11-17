package de.ppi.deepsampler.core.error;

import java.lang.reflect.Method;

public class InvalidMatcherConfigException extends BaseException {

    public InvalidMatcherConfigException(Method method) {
        super("The method %s has an invalid Matcher combination. " +
                "You must provide a Matcher either for all or for none of the parameters.", method.getName());
    }
}
