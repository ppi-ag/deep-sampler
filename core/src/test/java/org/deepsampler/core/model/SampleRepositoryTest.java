package org.deepsampler.core.model;

import org.deepsampler.core.api.Sampler;
import org.deepsampler.core.error.DuplicateSampleDefinitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
                Arrays.asList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );

        // WHEN
        SampleRepository.getInstance().add(registeredSampleDefinition);
        final SampledMethod foundSampledMethod = createSampledMethod(TestObject.class, "someMethod");

        // THEN
        final SampleDefinition expectedSampleDefinition =
                SampleRepository.getInstance().find(foundSampledMethod, "Argument");
        assertNotNull(expectedSampleDefinition);
    }

    @Test
    void sampleIsFoundOnSuperClass() throws NoSuchMethodException {
        // GIVEN
        final SampleDefinition registeredSampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                Arrays.asList(parameter -> parameter.equals("Argument")),
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
     * @throws NoSuchMethodException
     */
    @Test
    void sampleIsNotFoundByMethod() throws NoSuchMethodException {
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                Arrays.asList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );
        SampleRepository.getInstance().add(sampleDefinition);
        assertEquals(null, SampleRepository.getInstance().find(
                createSampledMethod(TestObject.class, "someMethod"), "someArg"));
    }

    /**
     * Tests {@link SampleRepository#find(SampledMethod, Object...)}
     * for {@link SampleDefinition} with different arguments.
     *
     * @throws NoSuchMethodException
     */
    @Test
    void sampleIsNotFoundByDifferentArgs() throws NoSuchMethodException {
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                Arrays.asList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );
        SampleRepository.getInstance().add(sampleDefinition);
        final SampledMethod lookupMethod = createSampledMethod(TestObject.class, "someMethod");
        assertEquals(null, SampleRepository.getInstance().find(lookupMethod, "otherArg"));
    }

    /**
     * Tests {@link SampleRepository#add(SampleDefinition)} for {@link DuplicateSampleDefinitionException}
     * with different {@link SampleDefinition}s.
     *
     * @throws NoSuchMethodException
     */
    @Test
    void duplicatedSampleInSamplesThrowsDSDException() throws NoSuchMethodException {
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "firstMethod"),
                Arrays.asList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );
        final SampleDefinition newCurrentSampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "secondMethod"),
                Arrays.asList(parameter -> parameter.equals("Other Argument")),
                "Other ReturnValue"
        );

        final SampleRepository sampleRepository = SampleRepository.getInstance();

        sampleRepository.add(sampleDefinition);
        sampleRepository.add(newCurrentSampleDefinition);

        assertThrows(DuplicateSampleDefinitionException.class,
                () ->  sampleRepository.add(sampleDefinition));
    }

    /**
     * Tests {@link SampleRepository#add(SampleDefinition)} for {@link DuplicateSampleDefinitionException}
     * with different {@link SampleDefinition}s.
     *
     * @throws NoSuchMethodException
     */
    @Test
    void duplicatedCurrentSampleThrowsDSDException() throws NoSuchMethodException {
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "firstMethod"),
                Arrays.asList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );

        final SampleRepository sampleRepository = SampleRepository.getInstance();
        sampleRepository.add(sampleDefinition);

        assertThrows(DuplicateSampleDefinitionException.class,
                () ->  sampleRepository.add(sampleDefinition));
    }

    @Test
    void getSamplesFromSampleRepository() throws NoSuchMethodException {
        final List<SampleDefinition> expectedSampleList = new ArrayList<>();
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "firstMethod"),
                Arrays.asList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );
        SampleRepository.getInstance().add(sampleDefinition);
        expectedSampleList.add(sampleDefinition);

        assertArrayEquals(expectedSampleList.toArray(), SampleRepository.getInstance().getSamples().toArray());
    }

    @Test
    void samplesListIsNotNullOrIsEmpty() {
        assertNotNull(SampleRepository.getInstance().getSamples());
        assertEquals(new ArrayList<SampleDefinition>().isEmpty(), SampleRepository.getInstance().isEmpty());
    }

    private SampleDefinition createSampleDefinition(
            final SampledMethod sampledMethod,
            final List<ParameterMatcher> parameter,
            final Object returnValue) throws NoSuchMethodException {

        final SampledMethod registeredSampledMethod = sampledMethod;
        final List<ParameterMatcher> registeredParameter = parameter;

        final SampleDefinition registeredSampleDefinition = new SampleDefinition(registeredSampledMethod);
        registeredSampleDefinition.setParameterMatchers(registeredParameter);
        registeredSampleDefinition.setReturnValueSupplier(() -> returnValue);
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