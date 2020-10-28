package org.deepsampler.core.api;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.deepsampler.core.error.InvalidMatcherConfigException;
import org.deepsampler.core.model.ParameterMatcher;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.deepsampler.core.api.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class MatchersTest {

    private static final Bean BEAN_A = new Bean("a", 1);
    private static final Bean BEAN_B = new Bean("b", 2);

    @BeforeEach
    void cleanUp() {
        Sampler.clear();
    }

    @Test
    void testSampleDefinitionWithAnyMatcher() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(any(Bean.class))).is(BEAN_A);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher> parameter = currentSampleDefinition.getParameterMatchers();

        assertEquals(1, parameter.size());
        assertAnyParameterMayAppear(0, BEAN_A, BEAN_B);
    }

    @Test
    void testSampleDefinitionWithMultipleAnyMatcher() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.methodWithMultipleParams(anyString(), any(Bean.class))).is("Some sampler");

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher> parameter = currentSampleDefinition.getParameterMatchers();

        assertEquals(2, parameter.size());
        assertAnyParameterMayAppear(0, "A random String", "Another random String");
        assertAnyParameterMayAppear(1, BEAN_A, BEAN_B);
    }

    @Test
    void testSampleDefinitionWithPrimitiveMatcher() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);

        Sample.of(testServiceSampler.echoBoolean(anyBoolean())).is(true);
        assertAnyParameterMayAppear(0, true, false);

        Sample.of(testServiceSampler.echoByte(anyByte())).is((byte) 1);
        assertAnyParameterMayAppear(0, 42, 43);

        Sample.of(testServiceSampler.echoChar(anyChar())).is('c');
        assertAnyParameterMayAppear(0, 'c', 'd');

        Sample.of(testServiceSampler.echoDouble(anyDouble())).is(3.1415);
        assertAnyParameterMayAppear(0, 42.0, 43.0);

        Sample.of(testServiceSampler.echoFloat(anyFloat())).is(3.1415f);
        assertAnyParameterMayAppear(0, 3.1415f, 3f);

        Sample.of(testServiceSampler.echoInt(anyInt())).is(42);
        assertAnyParameterMayAppear(0, 1, 2);

        Sample.of(testServiceSampler.echoShort(anyShort())).is((short) 1);
        assertAnyParameterMayAppear(0, 1, 2);
    }

    @Test
    void equalsMatcherWorks() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(equalTo(BEAN_A))).is(BEAN_B);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher> parameter = currentSampleDefinition.getParameterMatchers();

        assertEquals(1, parameter.size());
        assertTrue(parameter.get(0).matches(new Bean(BEAN_A.someString, BEAN_A.someInt)));
        assertFalse(parameter.get(0).matches(BEAN_B));
    }

    @Test
    void sameAsMatcherWorks() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(sameAs(BEAN_A))).is(BEAN_B);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher> parameter = currentSampleDefinition.getParameterMatchers();

        assertEquals(1, parameter.size());
        assertFalse(parameter.get(0).matches(new Bean(BEAN_A.someString, BEAN_A.someInt)));
        assertTrue(parameter.get(0).matches(BEAN_A));
    }

    @Test
    void mixedMatchersWork() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.methodWithMultipleParams(equalTo("Expected String"), sameAs(BEAN_A))).is("Some result");

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher> parameter = currentSampleDefinition.getParameterMatchers();

        assertEquals(2, parameter.size());
        assertTrue(parameter.get(0).matches("Expected String"));
        assertFalse(parameter.get(0).matches("Wrong String"));
        assertTrue(parameter.get(1).matches(BEAN_A));
        assertFalse(parameter.get(1).matches(new Bean(BEAN_A.someString, BEAN_A.someInt)));
    }


    private void assertAnyParameterMayAppear(final int parameterIndex, final Object alternativeOne, final Object alternativeTwo) {
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher> parameter = currentSampleDefinition.getParameterMatchers();

        assertTrue(parameter.get(parameterIndex).matches(alternativeOne));
        assertTrue(parameter.get(parameterIndex).matches(alternativeTwo));
        assertTrue(parameter.get(parameterIndex).matches(null));
    }



    @Test
    void mixedMatchersAreNotAllowed() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        assertThrows(InvalidMatcherConfigException.class,() -> mixMatchers(testServiceSampler));
    }

    private void mixMatchers(final TestService testServiceSampler) {
        Sample.of(testServiceSampler.methodWithMultipleParams(anyString(), BEAN_A)).is("Some sampler")
;    }

    static class TestService {

        Bean echoParameter(final Bean bean) {
            return bean;
        }

        @SuppressWarnings("unused")
        String methodWithMultipleParams(final String someString, final Bean someBean) {
            return "Some String";
        }

        int echoInt(final int someInt) {
            return someInt;
        }

        double echoDouble(final double someDouble) {
            return someDouble;
        }

        float echoFloat(final float someFloat) {
            return someFloat;
        }

        short echoShort(final short someShort) {
            return someShort;
        }

        char echoChar(final char someChar) {
            return someChar;
        }

        byte echoByte(final byte someByte) {
            return someByte;
        }

        boolean echoBoolean(final boolean someBoolean) {
            return someBoolean;
        }
    }

    static class Bean {
        private final String someString;
        private final int someInt;

        Bean(final String someString, final int someInt) {
            this.someString = someString;
            this.someInt = someInt;
        }

        @Override
        public boolean equals(final Object other) {
            return EqualsBuilder.reflectionEquals(other, this, true);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this, true);
        }
    }
}
