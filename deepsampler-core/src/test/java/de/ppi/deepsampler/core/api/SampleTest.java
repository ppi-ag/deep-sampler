/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.error.NotASamplerException;
import de.ppi.deepsampler.core.model.Answer;
import de.ppi.deepsampler.core.model.ParameterMatcher;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
    void testSampleDefinitionWithoutParam() throws Throwable {
        // GIVEN WHEN
        final TestService serviceSampler = Sampler.prepare(TestService.class);
        Sample.of(serviceSampler.noParameter()).is(STRING_SAMPLE);

        // THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();

        assertEquals(TestService.class, currentSampleDefinition.getSampledMethod().getTarget());
        assertTrue(currentSampleDefinition.getParameterMatchers().isEmpty());
        assertEquals(STRING_SAMPLE, currentSampleDefinition.getAnswer().call(null));
    }

    @Test
    void callOfANonSamplerIsDetectedIBeforeSamplerHasBeenDefined() {
        // GIVEN
        final TestService notASampler = new TestService();
        // THEN
        Sampler.clear();
        assertThrows(NotASamplerException.class, () -> shouldThrowExceptionAttemptingToSampleANonSampler(notASampler));
    }

    private void shouldThrowExceptionAttemptingToSampleANonSampler(TestService notASampler) {
        Sample.of(notASampler.echoParameter(PARAMETER_VALUE));
    }

    @Test
    void callOfAVoidNonSamplerIsDetectedIBeforeSamplerHasBeenDefined() {
        // GIVEN
        final TestService notASampler = new TestService();
        // THEN
        Sampler.clear();
        assertThrows(NotASamplerException.class, () -> Sample.of(notASampler::voidMethod));
    }

    @Test
    void callOfANonSamplerIsDetectedAfterSamplersHasBeenDefined() {
        //GIVEN
        Sampler.clear();
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        final TestService notASampler = new TestService();

        //WHEN UNCHANGED
        assertDoesNotThrow(() -> Sample.of(testServiceSampler.echoParameter(PARAMETER_VALUE)));

        // THEN
        assertThrows(NotASamplerException.class, () -> shouldThrowExceptionAttemptingToSampleANonSampler(notASampler));
    }

    @Test
    void callOfAVoidNonSamplerIsDetectedAfterSamplersHasBeenDefined() {
        //GIVEN
        Sampler.clear();
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        final TestService notASampler = new TestService();

        //WHEN UNCHANGED
        assertDoesNotThrow(() -> Sample.of(testServiceSampler.echoParameter(PARAMETER_VALUE)));

        // THEN
        assertThrows(NotASamplerException.class, () -> Sample.of(notASampler::voidMethod));
    }

    @Test
    void callOfANonSamplerIsDetectedAfterVoidSamplersHasBeenDefined() {
        //GIVEN
        Sampler.clear();
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        final TestService notASampler = new TestService();

        //WHEN
        Sample.of(testServiceSampler.echoParameter(PARAMETER_VALUE));
        Sample.of(testServiceSampler::voidMethod);

        // THEN
        assertThrows(NotASamplerException.class, () -> Sample.of(notASampler::voidMethod));
    }


    @Test
    void testSampleDefinitionWithPrimitiveParam() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(PARAMETER_VALUE)).is("New Sample");

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher<?>> parameter = currentSampleDefinition.getParameterMatchers();

        assertEquals(1, parameter.size());
        assertTrue(currentSampleDefinition.getParameterMatcherAs(0, String.class).matches(PARAMETER_VALUE));
    }

    @Test
    void testSampleDefinitionForInterface() {
        //GIVEN WHEN
        final TestServiceInterface testServiceSampler = Sampler.prepare(TestServiceInterface.class);
        Sample.of(testServiceSampler.echoParameter(PARAMETER_VALUE)).is(STRING_SAMPLE);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher<?>> parameter = currentSampleDefinition.getParameterMatchers();

        assertEquals(1, parameter.size());
        assertTrue(currentSampleDefinition.getParameterMatcherAs(0, String.class).matches(PARAMETER_VALUE));
    }


    @Test
    void testSampleDefinitionWithBeanParam() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(BEAN_A)).is(BEAN_B);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher<?>> parameter = currentSampleDefinition.getParameterMatchers();

        assertEquals(1, parameter.size());
        assertTrue(currentSampleDefinition.getParameterMatcherAs(0, Bean.class).matches(BEAN_A_COPY));
    }

    @Test
    void testSampleDefinitionWithArrayReturnValue() throws Throwable {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getArray()).is(new String[] {STRING_SAMPLE});

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final Answer<?> answer = currentSampleDefinition.getAnswer();

        assertEquals(1, ((String[]) answer.call(null)).length);
        assertEquals(STRING_SAMPLE, ((String[]) answer.call(null))[0]);
    }

    @Test
    void testSampleDefinitionWithPrimitiveReturnValues() throws Throwable {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);

        Sample.of(testServiceSampler.getInt()).is(1);
        assertEquals(1, getCurrentReturnValueSupplier().call(null));

        Sample.of(testServiceSampler.getFloat()).is(1.0f);
        assertEquals(1.0f, getCurrentReturnValueSupplier().call(null));
        Sample.of(testServiceSampler.getDouble()).is(1.0);
        assertEquals(1.0, getCurrentReturnValueSupplier().call(null));

        Sample.of(testServiceSampler.getChar()).is('c');
        assertEquals('c', getCurrentReturnValueSupplier().call(null));

        Sample.of(testServiceSampler.getByte()).is((byte) 1);
        assertEquals((byte) 1, getCurrentReturnValueSupplier().call(null));

        Sample.of(testServiceSampler.getShort()).is((short)1);
        assertEquals((short) 1, getCurrentReturnValueSupplier().call(null));
    }

    private Answer<?> getCurrentReturnValueSupplier() {
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        return currentSampleDefinition.getAnswer();
    }

    @Test
    void testSampleDefinitionWithLambda() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(BEAN_A)).answers(invocation -> BEAN_B);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher<?>> parameter = currentSampleDefinition.getParameterMatchers();

        assertEquals(1, parameter.size());
        assertTrue(currentSampleDefinition.getParameterMatcherAs(0, Bean.class).matches(BEAN_A_COPY));
    }

    @Test
    void exceptionCanBeThrownBySample() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(BEAN_A)).throwsException(Exception.class);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final Answer<?> answer = currentSampleDefinition.getAnswer();

        assertThrows(Exception.class, () -> answer.call(null));
    }

    @Test
    void runTimeExceptionCanBeThrownBySample() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(BEAN_A)).throwsException(new RuntimeException());

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final Answer<?> answer = currentSampleDefinition.getAnswer();

        assertThrows(Exception.class, () -> answer.call(null));
    }

    @Test
    void voidMethodThrowsAnExceptionBySample() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler::voidMethod).throwsException(Exception.class);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final Answer<?> answer = currentSampleDefinition.getAnswer();

        assertThrows(Exception.class, () -> answer.call(null));
    }

    @Test
    void voidMethodThrowsAnRuntimeExceptionBySample() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler::voidMethod).throwsException(new RuntimeException());

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final Answer<?> answer = currentSampleDefinition.getAnswer();

        assertThrows(RuntimeException.class, () -> answer.call(null));
    }


    @Test
    void callOfANonSamplerInsideOfAVoidCallIsDetected() {
        //GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);

        //WHEN UNCHANGED
        assertDoesNotThrow(() -> Sample.of(testServiceSampler::voidMethod));

        //THEN
        assertThrows(NotASamplerException.class, () -> Sample.of(() -> {}));
    }

    @Test
    void voidMethodsCanBeReplacedByVoidAnswers() throws Throwable {
        //GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);

        final AtomicInteger counter = new AtomicInteger(0);

        // WHEN
        Sample.of(testServiceSampler::voidMethod).answers(stubMethodInvocation -> counter.set(1));

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final Answer<?> answer = currentSampleDefinition.getAnswer();
        answer.call(null);

        assertEquals(1, counter.get());
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

        public void voidMethod() {
            //The behavior of this method is entirely determined by Sampl
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