package org.deepsampler.core.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SampleRepositoryTest {

    @Test
    public void sampleIsFound() throws NoSuchMethodException {
        // GIVEN
        SampledMethod registeredSampledMethod = createSampledMethod(TestObject.class);
        List<ParameterMatcher> registeredParameter = Arrays.asList(parameter -> parameter.equals("Argument"));

        SampleDefinition registeredSampleDefinition = new SampleDefinition(registeredSampledMethod);
        registeredSampleDefinition.setParameter(registeredParameter);
        registeredSampleDefinition.setReturnValueSupplier(() -> "ReturnValue");

        // WHEN
        SampleRepository.getInstance().add(registeredSampleDefinition);
        SampledMethod foundSampledMethod = createSampledMethod(TestObject.class);

        // THEN
        SampleDefinition expectedSampleDefinition = SampleRepository.getInstance().find(foundSampledMethod, "Argument");
        assertNotNull(expectedSampleDefinition);
    }

    @Test
    public void sampleIsFoundOnSuperClass() throws NoSuchMethodException {
        // GIVEN
        SampledMethod registeredSampledMethod = createSampledMethod(TestObject.class);
        List<ParameterMatcher> registeredParameter = Arrays.asList(parameter -> parameter.equals("Argument"));

        SampleDefinition registeredSampleDefinition = new SampleDefinition(registeredSampledMethod);
        registeredSampleDefinition.setParameter(registeredParameter);
        registeredSampleDefinition.setReturnValueSupplier(() -> "ReturnValue");

        // WHEN
        SampleRepository.getInstance().add(registeredSampleDefinition);
        SampledMethod foundSampledMethod = createSampledMethod(TestSubObject.class);

        // THEN
        SampleDefinition expectedSampleDefinition = SampleRepository.getInstance().find(foundSampledMethod, "Argument");
        assertNotNull(expectedSampleDefinition);
    }

    private SampledMethod createSampledMethod(Class<?> sampledClass) throws NoSuchMethodException {
        Method sampledMethod = sampledClass.getMethod("someMethod", String.class);
        return new SampledMethod(sampledClass, sampledMethod);
    }

    private static class TestObject {
        public String someMethod(String parameter) {
            return parameter;
        }
    }

    private static class TestSubObject extends TestObject {

    }


}