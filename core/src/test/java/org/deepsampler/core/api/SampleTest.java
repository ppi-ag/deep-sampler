package org.deepsampler.core.api;

import org.deepsampler.core.error.NotASamplerException;
import org.deepsampler.core.model.ParameterMatcher;
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

    @BeforeEach
    public void cleanUp() {
        Sampler.clear();
    }

    @Test
    void testSampleDefinitionWithoutParam() {
        // GIVEN WHEN
        final Quantity quantitySampler = Sampler.prepare(Quantity.class);
        Sample.of(quantitySampler.getTimes()).is(4);

        // THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();

        assertEquals(Quantity.class, currentSampleDefinition.getSampledMethod().getTarget());
        assertTrue(currentSampleDefinition.getParameter().isEmpty());
        assertEquals(4, currentSampleDefinition.getReturnValueSupplier().supply());
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
}