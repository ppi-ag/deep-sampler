package org.deepmock.core.api;

import org.deepmock.core.model.SampleDefinition;
import org.deepmock.core.model.SampleRepository;
import org.deepmock.core.model.ParameterMatcher;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class SampleTest {

    public static final String PARAMETER_VALUE = "Blubb";
    private static final Bean BEAN_A = new Bean("a", 1);
    private static final Bean BEAN_A_COPY = new Bean("a", 1);
    private static final Bean BEAN_B = new Bean("b", 2);

    @Test
    public void testSampleDefinitionWithoutParam() {
        // GIVEN WHEN
        Quantity quantitySampler = Sampler.prepare(Quantity.class);
        Sample.of(quantitySampler.getTimes()).is(4);

        // THEN
        SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        assertEquals(Quantity.class, currentSampleDefinition.getSampledMethod().getTarget());
        assertTrue(currentSampleDefinition.getParameter().isEmpty());
        assertEquals(4, currentSampleDefinition.getReturnValueSupplier().supply());
    }

    @Test
    public void testSampleDefinitionWithPrimitiveParam() {
        //GIVEN WHEN
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(PARAMETER_VALUE)).is("New Sample");

        //THEN
        SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        List<ParameterMatcher> parameter = currentSampleDefinition.getParameter();

        assertEquals(parameter.size(), 1);
        assertTrue(parameter.get(0).matches(PARAMETER_VALUE));
    }

    @Test
    public void testSampleDefinitionWithBeanParam() {
        //GIVEN WHEN
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(BEAN_A)).is(BEAN_B);

        //THEN
        SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        List<ParameterMatcher> parameter = currentSampleDefinition.getParameter();

        assertEquals(parameter.size(), 1);
        assertTrue(parameter.get(0).matches(BEAN_A_COPY));
    }

    public static class TestService {

        public String echoParameter(String someParameter) {
            return someParameter;
        }

        public Bean echoParameter(Bean bean) {
            return bean;
        }
    }

    public static class Bean {
        private String someString;
        private int someInt;

        public Bean(String someString, int someInt) {
            this.someString = someString;
            this.someInt = someInt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bean bean = (Bean) o;
            return someInt == bean.someInt &&
                    Objects.equals(someString, bean.someString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(someString, someInt);
        }
    }
}