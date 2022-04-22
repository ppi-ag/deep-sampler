/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.error.InvalidMatcherConfigException;
import de.ppi.deepsampler.core.model.ParameterMatcher;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static de.ppi.deepsampler.core.api.Matchers.EqualsMatcher;
import static de.ppi.deepsampler.core.api.Matchers.any;
import static de.ppi.deepsampler.core.api.Matchers.anyBoolean;
import static de.ppi.deepsampler.core.api.Matchers.anyByte;
import static de.ppi.deepsampler.core.api.Matchers.anyChar;
import static de.ppi.deepsampler.core.api.Matchers.anyDouble;
import static de.ppi.deepsampler.core.api.Matchers.anyFloat;
import static de.ppi.deepsampler.core.api.Matchers.anyInt;
import static de.ppi.deepsampler.core.api.Matchers.anyLong;
import static de.ppi.deepsampler.core.api.Matchers.anyShort;
import static de.ppi.deepsampler.core.api.Matchers.anyString;
import static de.ppi.deepsampler.core.api.Matchers.equalTo;
import static de.ppi.deepsampler.core.api.Matchers.sameAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertEquals(1, currentSampleDefinition.getNumberOfParameters());
        assertAnyParameterMayAppear(0, BEAN_A, BEAN_B);
    }

    @Test
    void testSampleDefinitionWithMultipleAnyMatcher() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.methodWithMultipleParams(anyString(), any(Bean.class))).is("Some sampler");

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();

        assertEquals(2, currentSampleDefinition.getNumberOfParameters());
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

        Sample.of(testServiceSampler.echoLong(anyLong())).is(42L);
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

        assertEquals(1, currentSampleDefinition.getNumberOfParameters());
        assertTrue(currentSampleDefinition.getParameterMatcherAs(0, Bean.class).matches(new Bean(BEAN_A.someString, BEAN_A.someInt)));
        assertFalse(currentSampleDefinition.getParameterMatcherAs(0, Bean.class).matches(BEAN_B));
    }

    @Test
    void equalsMatcherWithStringWorks() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoString("make it so!")).is("engage!");

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();

        assertEquals(1, currentSampleDefinition.getNumberOfParameters());
        assertTrue(currentSampleDefinition.getParameterMatcherAs(0, String.class).matches("make it so!"));
    }

    @Test
    void explicitEqualsMatcherWithStringWorks() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoString(Matchers.equalTo("make it so!"))).is("engage!");

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();

        assertEquals(1, currentSampleDefinition.getNumberOfParameters());
        assertTrue(currentSampleDefinition.getParameterMatcherAs(0, String.class).matches("make it so!"));
    }

    @Test
    void sameAsMatcherWorks() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(sameAs(BEAN_A))).is(BEAN_B);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();

        assertEquals(1, currentSampleDefinition.getNumberOfParameters());
        assertFalse(currentSampleDefinition.getParameterMatcherAs(0, Bean.class).matches(new Bean(BEAN_A.someString, BEAN_A.someInt)));
        assertTrue(currentSampleDefinition.getParameterMatcherAs(0, Bean.class).matches(BEAN_A));
    }

    @Test
    void mixedMatchersWork() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.methodWithMultipleParams(equalTo("Expected String"), sameAs(BEAN_A))).is("Some result");

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();

        assertEquals(2, currentSampleDefinition.getParameterMatchers().size());
        assertTrue(currentSampleDefinition.getParameterMatcherAs(0, String.class).matches("Expected String"));
        assertFalse(currentSampleDefinition.getParameterMatcherAs(0, String.class).matches("Wrong String"));
        assertTrue(currentSampleDefinition.getParameterMatcherAs(1, Bean.class).matches(BEAN_A));
        assertFalse(currentSampleDefinition.getParameterMatcherAs(1, Bean.class).matches(new Bean(BEAN_A.someString, BEAN_A.someInt)));
    }


    private void assertAnyParameterMayAppear(final int parameterIndex, final Object alternativeOne, final Object alternativeTwo) {
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();

        assertTrue(currentSampleDefinition.getParameterMatcherAs(parameterIndex, Object.class).matches(alternativeOne));
        assertTrue(currentSampleDefinition.getParameterMatcherAs(parameterIndex, Object.class).matches(alternativeTwo));
        assertTrue(currentSampleDefinition.getParameterMatcherAs(parameterIndex, Object.class).matches(null));
    }



    @Test
    void mixedMatchersAreNotAllowed() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        assertThrows(InvalidMatcherConfigException.class,() -> mixMatchers(testServiceSampler));
    }

    private void mixMatchers(final TestService testServiceSampler) {
        Sample.of(testServiceSampler.methodWithMultipleParams(anyString(), BEAN_A)).is("Some sampler");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCustomMatcher() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.methodWithMultipleParams(Matchers.matcher(new ContainsMatcher("ABC")), any(Bean.class)))
                .is("Some result");

        // THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher<?>> parameter = currentSampleDefinition.getParameterMatchers();

        assertEquals(2, parameter.size());
        assertTrue(((ParameterMatcher<String>)parameter.get(0)).matches("XABCX"));
        assertFalse(((ParameterMatcher<String>)parameter.get(0)).matches("ABDX"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void parameterWithoutEqualsIsDetected() {
        //GIVEN WHEN
        final BeanWithoutEquals beanWithoutEquals = new BeanWithoutEquals();
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.provokeMissingEqualsException(beanWithoutEquals)).is(BEAN_A);

        // THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher<?>> parameter = currentSampleDefinition.getParameterMatchers();

        final ParameterMatcher<BeanWithoutEquals> matcher = (ParameterMatcher<BeanWithoutEquals>) parameter.get(0);

        assertThrows(InvalidConfigException.class, () -> matcher.matches(beanWithoutEquals));
    }

    @Test
    @SuppressWarnings("unchecked")
    void equalsMatcherAllowsEqualsMethodInSuperType() {
        // GIVEN WHEN
        final BeanWithInheritedEquals bean = new BeanWithInheritedEquals("a", 1);
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.acceptInheritedEquals(bean)).is(BEAN_A);

        //THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher<?>> parameter = currentSampleDefinition.getParameterMatchers();

        final EqualsMatcher<BeanWithInheritedEquals> matcher = (EqualsMatcher<BeanWithInheritedEquals>) parameter.get(0);

        assertTrue(matcher.matches(bean));
    }

    static class ContainsMatcher implements ParameterMatcher<String> {
        private final String containedStr;

        public ContainsMatcher(final String containedStr) {
            this.containedStr = containedStr;
        }

        @Override
        public boolean matches(final String parameter) {
            return parameter.contains(containedStr);
        }
    }

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

        long echoLong(final long someLong) {
            return someLong;
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

        String echoString(final String string) {
            return string;
        }

        boolean echoBoolean(final boolean someBoolean) {
            return someBoolean;
        }

        @SuppressWarnings("unused")
        Bean provokeMissingEqualsException(final BeanWithoutEquals parameter) { return null; }

        Bean acceptInheritedEquals(final BeanWithInheritedEquals bean){
            return null;
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

    static class BeanWithoutEquals {

    }

    static class BeanWithInheritedEquals extends Bean {

        BeanWithInheritedEquals(final String someString, final int someInt) {
            super(someString, someInt);
        }
    }
}
