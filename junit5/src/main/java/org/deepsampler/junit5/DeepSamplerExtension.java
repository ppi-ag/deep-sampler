package org.deepsampler.junit5;

import org.deepsampler.core.api.Sampler;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class DeepSamplerExtension implements TestInstancePostProcessor, BeforeEachCallback {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        getDeclaredAndInheritedFields(testInstance.getClass()).stream()//
            .filter(this::shouldBeSampled)//
            .forEach(field -> assignNewSamplerToField(testInstance, field));
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Sampler.clear();
    }

    private List<Field> getDeclaredAndInheritedFields(Class<?> clazz) {
        List<Field> declaredFields = Arrays.asList(clazz.getDeclaredFields());

        if (!Object.class.equals(clazz.getSuperclass())) {
            declaredFields.addAll(getDeclaredAndInheritedFields(clazz.getSuperclass()));
        }

        return declaredFields;
    }

    private boolean shouldBeSampled(Field field) {
        return field.getAnnotation(PrepareSampler.class) != null;
    }

    private void assignNewSamplerToField(Object testInstance, Field field) {
        Object sampler = Sampler.prepare(field.getType());
        try {
            field.setAccessible(true);
            field.set(testInstance, sampler);
        } catch (IllegalAccessException e) {
            throw new JUnitPreparationException("No access to property %s#%s", e, testInstance.getClass().getName(), field.getName());
        }
    }


}
