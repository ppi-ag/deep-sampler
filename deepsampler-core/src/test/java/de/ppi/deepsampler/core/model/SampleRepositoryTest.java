/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import de.ppi.deepsampler.core.api.Sampler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SampleRepositoryTest {

    /**
     * Clear {@link Sampler}.
     */
    @BeforeEach
    public void clearSampler() {
        Sampler.clear();
    }

    @Test
    void sampleIsFound() throws NoSuchMethodException {
        // GIVEN
        final SampleDefinition registeredSampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                Collections.singletonList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );

        // WHEN
        SampleRepository.getInstance().add(registeredSampleDefinition);
        final SampledMethod foundSampledMethod = createSampledMethod(TestObject.class, "someMethod");

        // THEN
        final SampleDefinition expectedSampleDefinition =
                SampleRepository.getInstance().find(foundSampledMethod, "Argument");
        assertNotNull(expectedSampleDefinition);
        assertEquals(registeredSampleDefinition, expectedSampleDefinition);
    }

    @Test
    void sampleIsFoundOnSuperClass() throws NoSuchMethodException {
        // GIVEN
        final SampleDefinition registeredSampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                Collections.singletonList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );

        // WHEN
        SampleRepository.getInstance().add(registeredSampleDefinition);
        final SampledMethod foundSampledMethod = createSampledMethod(TestSubObject.class, "someMethod");

        // THEN
        final SampleDefinition expectedSampleDefinition =
                SampleRepository.getInstance().find(foundSampledMethod, "Argument");
        assertNotNull(expectedSampleDefinition);
    }

    /**
     * Tests {@link SampleRepository#find(SampledMethod, Object...)}
     * for {@link SampleDefinition} with different {@link SampledMethod}.
     *
     * @throws NoSuchMethodException NoSuchMethodException
     */
    @Test
    void sampleIsNotFoundByMethod() throws NoSuchMethodException {
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                Collections.singletonList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );
        SampleRepository.getInstance().add(sampleDefinition);
        assertNull(SampleRepository.getInstance().find(
                createSampledMethod(TestObject.class, "someMethod"), "someArg"));
    }

    /**
     * Tests {@link SampleRepository#find(SampledMethod, Object...)}
     * for {@link SampleDefinition} with different arguments.
     *
     * @throws NoSuchMethodException NoSuchMethodException
     */
    @Test
    void sampleIsNotFoundByDifferentArgs() throws NoSuchMethodException {
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                Collections.singletonList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );
        SampleRepository.getInstance().add(sampleDefinition);
        final SampledMethod lookupMethod = createSampledMethod(TestObject.class, "someMethod");
        assertNull(SampleRepository.getInstance().find(lookupMethod, "otherArg"));
    }

    @Test
    void getSamplesFromSampleRepository() throws NoSuchMethodException {
        final List<SampleDefinition> expectedSampleList = new ArrayList<>();
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "firstMethod"),
                Collections.singletonList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );
        SampleRepository.getInstance().add(sampleDefinition);
        expectedSampleList.add(sampleDefinition);

        assertArrayEquals(expectedSampleList.toArray(), SampleRepository.getInstance().getSamples().toArray());
    }

    @Test
    void samplesListIsNotNullOrIsEmpty() {
        assertNotNull(SampleRepository.getInstance().getSamples());
        assertTrue(SampleRepository.getInstance().isEmpty());
    }

    private SampleDefinition createSampleDefinition(
            final SampledMethod sampledMethod,
            final List<ParameterMatcher<?>> parameter,
            final Object returnValue) {

        final SampleDefinition registeredSampleDefinition = new SampleDefinition(sampledMethod);
        registeredSampleDefinition.setParameterMatchers(parameter);
        registeredSampleDefinition.setAnswer(invocation -> returnValue);
        return registeredSampleDefinition;
    }

    private SampledMethod createSampledMethod(
            final Class<?> sampledClass,
            final String name) throws NoSuchMethodException {

        final Method sampledMethod = sampledClass.getMethod(name, String.class);
        return new SampledMethod(sampledClass, sampledMethod);
    }

    private static class TestObject {
        public String someMethod(final String parameter) {
            return parameter;
        }
        public String firstMethod(final String parameter) {
            return parameter;
        }
        public String secondMethod(final String parameter) {
            return parameter;
        }
    }

    private static class TestSubObject extends TestObject {

    }


}