/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.internal.aophandler;

import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;

import java.lang.reflect.Method;

/**
 * A {@link javassist.util.proxy.MethodHandler} that taps into method calls and adds each method call as a {@link SampleDefinition}
 * to the {@link SampleRepository}. This is trick how DeepSampler is able to define the stubbed methods by calling the methods
 * that should be stubbed. (@see {@link de.ppi.deepsampler.core.api.Sampler}).
 *
 * Methods of the class {@link Object} are ignored. Otherwise strange effects might appear, e.g. if Object::finalize is
 * called by the garbage collector.
 */
public class RecordSampleHandler extends ReturningSampleHandler {
    private final Class<?> cls;

    public RecordSampleHandler(final Class<?> cls) {
        this.cls = cls;
    }

    @Override
    public Object invoke(final Object self, final Method method, final Method proceed, final Object[] args) {
        if (!Object.class.equals(method.getDeclaringClass())) {
            final var sampleDefinition = createSampleDefinition(cls, method, args);
            SampleRepository.getInstance().add(sampleDefinition);
            SampleRepository.getInstance().setMarkNextVoidSamplerForPersistence(false);
        }

        return createEmptyProxy(method.getReturnType());
    }


}
