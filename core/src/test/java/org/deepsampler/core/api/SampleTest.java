package org.deepsampler.core.api;

import org.deepsampler.core.error.NotASamplerException;
import org.deepsampler.core.model.ParameterMatcher;
import org.deepsampler.core.model.ReturnValueSupplier;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class SampleTest {

    public static final String PARAMETER_VALUE = "Blubb";
    private static final Bean BEAN_A = new Bean("a", 1);
    private static final Bean BEAN_A_COPY = new Bean("a", 1);
    private static final Bean BEAN_B = new Bean("b", 2);
    public static final String STRING_SAMPLE = "Sampled";

    @BeforeEach
    public void cleanUp() {
        Sampler.clear();
    }

    @Test
    void testSampleDefinitionWithoutParam() {
        // GIVEN WHEN
        final TestService serviceSampler = Sampler.prepare(TestService.class);
        Sample.of(serviceSampler.noParameter()).is(STRING_SAMPLE);

        // THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();

        assertEquals(TestService.class, currentSampleDefinition.getSampledMethod().getTarget());
        assertTrue(currentSampleDefinition.getParameter().isEmpty());
        assertEquals(STRING_SAMPLE, currentSampleDefinition.getReturnValueSupplier().supply());
    }

    @Test
    void testSampleDefinitionWithPrimitiveParam() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(PARAMETER_VALUE)).is("New Sample");

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher> parameter = currentSampleDefinition.getParameter();

        assertEquals(1, parameter.size());
        assertTrue(parameter.get(0).matches(PARAMETER_VALUE));
    }


    void testSampleDefinitionForInterface() {
        //GIVEN WHEN
        final TestServiceInterface testServiceSampler = Sampler.prepare(TestServiceInterface.class);
        Sample.of(testServiceSampler.echoParameter(PARAMETER_VALUE)).is(STRING_SAMPLE);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher> parameter = currentSampleDefinition.getParameter();

        assertEquals(1, parameter.size());
        assertTrue(parameter.get(0).matches(PARAMETER_VALUE));
    }


    @Test
    void testSampleDefinitionWithBeanParam() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(BEAN_A)).is(BEAN_B);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher> parameter = currentSampleDefinition.getParameter();

        assertEquals(1, parameter.size());
        assertTrue(parameter.get(0).matches(BEAN_A_COPY));
    }

    @Test
    void testSampleDefinitionWithArrayReturnValue() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getArray()).is(new String[] {STRING_SAMPLE});

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final ReturnValueSupplier returnValueSupplier = currentSampleDefinition.getReturnValueSupplier();

        assertEquals(1, ((String[]) returnValueSupplier.supply()).length);
        assertEquals(STRING_SAMPLE, ((String[]) returnValueSupplier.supply())[0]);
    }

    @Test
    void testSampleDefinitionWithPrimitiveReturnValues() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);

        Sample.of(testServiceSampler.getInt()).is(1);
        assertEquals(1, getCurrentReturnValueSupplier().supply());

        Sample.of(testServiceSampler.getFloat()).is(1.0f);
        assertEquals(1.0f, getCurrentReturnValueSupplier().supply());
        Sample.of(testServiceSampler.getDouble()).is(1.0);
        assertEquals(1.0, getCurrentReturnValueSupplier().supply());

        Sample.of(testServiceSampler.getChar()).is('c');
        assertEquals('c', getCurrentReturnValueSupplier().supply());

        Sample.of(testServiceSampler.getByte()).is((byte) 1);
        assertEquals((byte) 1, getCurrentReturnValueSupplier().supply());

        Sample.of(testServiceSampler.getShort()).is((short)1);
        assertEquals((short) 1, getCurrentReturnValueSupplier().supply());
    }

    private ReturnValueSupplier getCurrentReturnValueSupplier() {
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        return currentSampleDefinition.getReturnValueSupplier();
    }

    @Test
    void testSampleDefinitionWithLambda() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(BEAN_A)).is(() -> BEAN_B);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher> parameter = currentSampleDefinition.getParameter();

        assertEquals(1, parameter.size());
        assertTrue(parameter.get(0).matches(BEAN_A_COPY));
    }

    @Test
    void samplerForVerificationIsChecked() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.forVerification(testServiceSampler);

        // THEN
        assertThrows(NotASamplerException.class, () -> Sample.forVerification("I'm not a Sampler."));
        assertThrows(NullPointerException.class, () -> Sample.forVerification(null));
    }


    public static class TestService {

        public String echoParameter(final String someParameter) {
            return someParameter;
        }

        public Bean echoParameter(final Bean bean) {
            return bean;
        }

        public String[] getArray() {
            return new String[] {"Some String"};
        }

        public int getInt() {
            return 1;
        }

        public float getFloat() {
            return 1.0f;
        }

        public double getDouble() {
            return 1.0;
        }

        public char getChar() {
            return 'c';
        }

        public byte getByte() {
            return 1;
        }

        public short getShort() {
            return (short) 1;
        }

        public String noParameter() {
            return "Hello Sample";
        }
    }

    public static class Bean {
        private final String someString;
        private final int someInt;

        public Bean(final String someString, final int someInt) {
            this.someString = someString;
            this.someInt = someInt;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Bean bean = (Bean) o;
            return someInt == bean.someInt &&
                    Objects.equals(someString, bean.someString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(someString, someInt);
        }
    }

    public interface TestServiceInterface {
        String echoParameter(String parameter);
    }
}