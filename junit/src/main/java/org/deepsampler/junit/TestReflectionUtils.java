package org.deepsampler.junit;

import org.deepsampler.core.api.Sampler;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides utilities for dealing with reflection in order to find prepare Sampler in Testcases using the Annotation {@link PrepareSampler}.
 */
public class TestReflectionUtils {

    /**
     * Finds all properties of clazz including inherited properties.
     *
     * @param clazz The clazz of which the properties are needed.
     * @return A Stream containing the found Fields.
     */
    public static Stream<Field> getDeclaredAndInheritedFields(Class<?> clazz) {
        Stream<Field> declaredFields = Arrays.stream(clazz.getDeclaredFields());

        if (!Object.class.equals(clazz.getSuperclass())) {
            return Stream.concat(declaredFields, getDeclaredAndInheritedFields(clazz.getSuperclass()));
        }

        return declaredFields;
    }

    /**
     * Checks whether a Field is annotated with {@link PrepareSampler} or not.
     *
     * @param field The field that is suspected to be annotated with {@link PrepareSampler}.
     * @return <code>true</code> if field is annotated with {@link PrepareSampler}
     */
    public static boolean shouldBeSampled(Field field) {
        return field.getAnnotation(PrepareSampler.class) != null;
    }

    /**
     * Prepares a new Sampler according to the type of field and assigns the Sampler the field on testInstance.
     *
     * @param testInstance the object in which the sampler should be injected.
     * @param field the field that should be populated with a new Sampler.
     */
    public static void assignNewSamplerToField(Object testInstance, Field field) {
        Object sampler = Sampler.prepare(field.getType());
        try {
            field.setAccessible(true);
            field.set(testInstance, sampler);
        } catch (IllegalAccessException e) {
            throw new JUnitPreparationException("No access to property %s#%s", e, testInstance.getClass().getName(), field.getName());
        }
    }
}
