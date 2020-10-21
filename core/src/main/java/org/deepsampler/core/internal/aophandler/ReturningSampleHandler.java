package org.deepsampler.core.internal.aophandler;

import javassist.util.proxy.MethodHandler;
import org.deepsampler.core.api.Matchers;
import org.deepsampler.core.error.InvalidConfigException;
import org.deepsampler.core.model.ParameterMatcher;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampledMethod;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ReturningSampleHandler implements MethodHandler {

    protected SampleDefinition createSampleDefinition(final Class<?> cls, final Method method, final Object[] args) {
        final SampledMethod sampledMethod = new SampledMethod(cls, method);
        final SampleDefinition sampleDefinition = new SampleDefinition(sampledMethod);

        final List<ParameterMatcher> parameterMatchers = Arrays.stream(args)
                .map(this::toMatcher)
                .collect(Collectors.toList());

        sampleDefinition.setParameter(parameterMatchers);
        sampleDefinition.setParameterValues(new ArrayList<>(Arrays.asList(args)));
        return sampleDefinition;
    }

    private ParameterMatcher toMatcher(final Object parameterValue) {
        if (parameterValue instanceof ParameterMatcher) {
            return (ParameterMatcher) parameterValue;
        } else {
            return Matchers.equalTo(parameterValue);
        }
    }

    protected Object createEmptyProxy(final Class<?> cls) {
        if (cls.isPrimitive()) {
            return createEmptyPrimitive(cls);
        } else if (cls.isArray()) {
            return createEmptyArray(cls);
        }
        return null;
    }

    private Object createEmptyArray(final Class<?> cls) {
        return Array.newInstance(cls.getComponentType(), 0);
    }

    @SuppressWarnings("UnnecessaryBoxing")
    private Object createEmptyPrimitive(final Class<?> cls) {
        if (cls.isAssignableFrom(int.class)) {
            return Integer.valueOf(0);
        } else if (cls.isAssignableFrom(double.class)) {
            return Double.valueOf(0.0);
        } else if (cls.isAssignableFrom(float.class)) {
            return Float.valueOf(0.0f);
        } else if (cls.isAssignableFrom(short.class)) {
            return Short.valueOf((short) 0);
        } else if (cls.isAssignableFrom(byte.class)) {
            return Byte.valueOf((byte) 0);
        } else if (cls.isAssignableFrom(char.class)) {
            return Character.valueOf('0');
        } else if (cls.isAssignableFrom(void.class)) {
            return null;
        }

        throw new InvalidConfigException("The unknown primitive '" + cls + "' appeared");
    }
}
