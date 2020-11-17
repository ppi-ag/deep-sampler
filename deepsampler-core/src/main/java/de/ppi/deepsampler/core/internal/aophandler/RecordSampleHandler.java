/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.internal.aophandler;

import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;

import java.lang.reflect.Method;

public class RecordSampleHandler extends ReturningSampleHandler {
    private final Class<?> cls;

    public RecordSampleHandler(final Class<?> cls) {
        this.cls = cls;
    }

    @Override
    public Object invoke(final Object self, final Method method, final Method proceed, final Object[] args) {
        final SampleDefinition sampleDefinition = createSampleDefinition(cls, method, args);

        SampleRepository.getInstance().add(sampleDefinition);

        return createEmptyProxy(method.getReturnType());
    }




}
