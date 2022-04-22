/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;

import de.ppi.deepsampler.core.api.Sample;
import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.model.ParameterMatcher;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecorded;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecordedBoolean;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecordedByte;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecordedChar;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecordedDouble;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecordedFloat;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecordedInt;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecordedLong;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecordedShort;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecordedString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistentMatchersTest {

    @Test
    void testSampleDefinitionWithPrimitiveMatcher() {
        //GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);

        Sample.of(testServiceSampler.echoBoolean(anyRecordedBoolean())).is(true);
        assertAnyParameterMayAppear(0, true, false);

        Sample.of(testServiceSampler.echoByte(anyRecordedByte())).is((byte) 1);
        assertAnyParameterMayAppear(0, 42, 43);

        Sample.of(testServiceSampler.echoChar(anyRecordedChar())).is('c');
        assertAnyParameterMayAppear(0, 'c', 'd');

        Sample.of(testServiceSampler.echoDouble(anyRecordedDouble())).is(3.1415);
        assertAnyParameterMayAppear(0, 42.0, 43.0);

        Sample.of(testServiceSampler.echoFloat(anyRecordedFloat())).is(3.1415f);
        assertAnyParameterMayAppear(0, 3.1415f, 3f);

        Sample.of(testServiceSampler.echoInt(anyRecordedInt())).is(42);
        assertAnyParameterMayAppear(0, 1, 2);

        Sample.of(testServiceSampler.echoLong(anyRecordedLong())).is(42L);
        assertAnyParameterMayAppear(0, 1, 2);

        Sample.of(testServiceSampler.echoShort(anyRecordedShort())).is((short) 1);
        assertAnyParameterMayAppear(0, 1, 2);

        Sample.of(testServiceSampler.echoString(anyRecordedString())).is("Picard");
        assertAnyParameterMayAppear(0, "Primary directive", "ignored");
    }

    @Test
    @SuppressWarnings("unchecked")
    void parameterWithoutEqualsThrowsException() {
        // ✋ GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);

        // 🧪 WHEN
        Sample.of(testServiceSampler.provokeMissingEqualsException(anyRecorded())).is(null);

        // 🔬 THEN
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        final List<ParameterMatcher<?>> parameter = currentSampleDefinition.getParameterMatchers();

        final ComboMatcher<BeanWithoutEquals> matcher = (ComboMatcher<BeanWithoutEquals>) parameter.get(0);

        assertThrows(InvalidConfigException.class, () -> matcher.getPersistentMatcher().matches(new BeanWithoutEquals(), new BeanWithoutEquals()));
    }


    private void assertAnyParameterMayAppear(final int parameterIndex, final Object alternativeOne, final Object alternativeTwo) {
        final SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();

        assertTrue(currentSampleDefinition.getParameterMatcherAs(parameterIndex, Object.class).matches(alternativeOne));
        assertTrue(currentSampleDefinition.getParameterMatcherAs(parameterIndex, Object.class).matches(alternativeTwo));
        assertTrue(currentSampleDefinition.getParameterMatcherAs(parameterIndex, Object.class).matches(null));
    }


    static class TestService {

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

        Bean provokeMissingEqualsException(final BeanWithoutEquals parameter) { return null; }
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

}
