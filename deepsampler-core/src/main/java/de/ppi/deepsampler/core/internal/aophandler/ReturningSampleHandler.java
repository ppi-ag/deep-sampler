/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.internal.aophandler;

import javassist.util.proxy.MethodHandler;
import de.ppi.deepsampler.core.api.Matchers;
import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.error.InvalidMatcherConfigException;
import de.ppi.deepsampler.core.model.ParameterMatcher;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.core.model.SampledMethod;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ReturningSampleHandler implements MethodHandler {

    protected SampleDefinition createSampleDefinition(final Class<?> cls, final Method method, final Object[] args) {
        List<ParameterMatcher<?>> parameterMatchers = collectMatchersForParameters(method, args);

        final SampledMethod sampledMethod = new SampledMethod(cls, method);
        final SampleDefinition sampleDefinition = new SampleDefinition(sampledMethod);

        sampleDefinition.setParameterMatchers(parameterMatchers);
        sampleDefinition.setParameterValues(new ArrayList<>(Arrays.asList(args)));
        sampleDefinition.setMarkedForPersistence(SampleRepository.getInstance().getMarkNextVoidSamplerForPersistence());

        return sampleDefinition;
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
        } else if (cls.isAssignableFrom(boolean.class)) {
            return Boolean.valueOf(true);
        } else if (cls.isAssignableFrom(void.class)) {
            return null;
        }

        throw new InvalidConfigException("The unknown primitive '" + cls + "' appeared");
    }

    private List<ParameterMatcher<?>> collectMatchersForParameters(Method method, Object[] parameters) {
        List<ParameterMatcher<?>> currentParameterMatchers = SampleRepository.getInstance().getCurrentParameterMatchers();

        SampleRepository.getInstance().clearCurrentParameterMatchers();

        if (!currentParameterMatchers.isEmpty() && currentParameterMatchers.size() != parameters.length) {
            throw new InvalidMatcherConfigException(method);
        }

        if (currentParameterMatchers.isEmpty()) {
            currentParameterMatchers = Arrays.stream(parameters)
                    .map(Matchers.EqualsMatcher::new)
                    .collect(Collectors.toList());
        }

        return currentParameterMatchers;
    }

}
