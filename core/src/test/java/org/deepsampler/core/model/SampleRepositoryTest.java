package org.deepsampler.core.model;

import org.deepsampler.core.api.Sampler;
import org.deepsampler.core.error.DuplicateSampleDefinitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SampleRepositoryTest {

    /**
     * Clear {@link Sampler}.
     */
    @BeforeEach
    public void clearSampler() {
        Sampler.clear();
    }

    @Test
    public void sampleIsFound() throws NoSuchMethodException {
        // GIVEN
        final SampleDefinition registeredSampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                TestObject.class,
                Arrays.asList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );

        // WHEN
        SampleRepository.getInstance().add(registeredSampleDefinition);
        final SampledMethod foundSampledMethod = createSampledMethod(TestObject.class, "someMethod");

        // THEN
        final SampleDefinition expectedSampleDefinition = SampleRepository.getInstance().find(foundSampledMethod, "Argument");
        assertNotNull(expectedSampleDefinition);
    }

    @Test
    public void sampleIsFoundOnSuperClass() throws NoSuchMethodException {
        // GIVEN
        final SampleDefinition registeredSampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                TestObject.class,
                Arrays.asList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );

        // WHEN
        SampleRepository.getInstance().add(registeredSampleDefinition);
        final SampledMethod foundSampledMethod = createSampledMethod(TestSubObject.class, "someMethod");

        // THEN
        final SampleDefinition expectedSampleDefinition = SampleRepository.getInstance().find(foundSampledMethod, "Argument");
        assertNotNull(expectedSampleDefinition);
    }

    /**
     * Tests {@link SampleRepository#find(SampledMethod, Object...)}
     * for {@link SampleDefinition} with different {@link SampledMethod}.
     *
     * @throws NoSuchMethodException
     */
    @Test
    public void sampleIsNotFoundByMethod() throws NoSuchMethodException {
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                TestObject.class,
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
    public void sampleIsNotFoundByDifferentArgs() throws NoSuchMethodException {
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "someMethod"),
                TestObject.class,
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
    public void duplicatedSampleInSamplesThrowsDSDException() throws NoSuchMethodException {
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "firstMethod"),
                TestObject.class,
                Arrays.asList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );
        final SampleDefinition newCurrentSampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "secondMethod"),
                TestObject.class,
                Arrays.asList(parameter -> parameter.equals("Other Argument")),
                "Other ReturnValue"
        );

        SampleRepository.getInstance().add(sampleDefinition);
        SampleRepository.getInstance().add(newCurrentSampleDefinition);

        assertThrows(DuplicateSampleDefinitionException.class, () ->  SampleRepository.getInstance().add(sampleDefinition));
    }

    /**
     * Tests {@link SampleRepository#add(SampleDefinition)} for {@link DuplicateSampleDefinitionException}
     * with different {@link SampleDefinition}s.
     *
     * @throws NoSuchMethodException
     */
    @Test
    public void duplicatedCurrentSampleThrowsDSDException() throws NoSuchMethodException {
        final SampleDefinition sampleDefinition = createSampleDefinition(
                createSampledMethod(TestObject.class, "firstMethod"),
                TestObject.class,
                Arrays.asList(parameter -> parameter.equals("Argument")),
                "ReturnValue"
        );
        SampleRepository.getInstance().add(sampleDefinition);

        assertThrows(DuplicateSampleDefinitionException.class, () ->  SampleRepository.getInstance().add(sampleDefinition));
    }

    private SampleDefinition createSampleDefinition(final SampledMethod sampledMethod, final Class clazz,
                                                    final List<ParameterMatcher> parameter, final Object returnValue) throws NoSuchMethodException {
        final SampledMethod registeredSampledMethod = sampledMethod;
        final List<ParameterMatcher> registeredParameter = parameter;

        final SampleDefinition registeredSampleDefinition = new SampleDefinition(registeredSampledMethod);
        registeredSampleDefinition.setParameter(registeredParameter);
        registeredSampleDefinition.setReturnValueSupplier(() -> returnValue);
        return registeredSampleDefinition;
    }

    private SampledMethod createSampledMethod(final Class<?> sampledClass, final String name) throws NoSuchMethodException {
        final Method sampledMethod = sampledClass.getMethod(name, String.class);
        return new SampledMethod(sampledClass, sampledMethod);
    }

    private static class TestObject {
        public String someMethod(String parameter) {
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