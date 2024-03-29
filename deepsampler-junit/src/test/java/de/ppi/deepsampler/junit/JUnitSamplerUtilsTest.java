/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitSamplerUtilsTest {

    @Test
    void declaredAndInheritedFieldsAreFound() {
        // DO
        final Stream<Field> declaredAndInheritedFieldStream = JUnitSamplerUtils.getDeclaredAndInheritedFields(SubTestBean.class);

        // Since the order in which reflection iterates over fields is not defined and may change, we sort the list
        // so selecting expected Fields using indexes is save. We also need to filter some proxy-fields that are introduced by jacoco
        final List<Field> declaredAndInheritedFields = declaredAndInheritedFieldStream.sorted(this::sortByName)//
                .filter(field -> !field.getName().startsWith("$")) //
                .collect(Collectors.toList());

        // THEN EXPECT
        assertEquals(3, declaredAndInheritedFields.size());
        assertEquals("aRandomIntField", declaredAndInheritedFields.get(0).getName());
        assertEquals("aRandomStringField", declaredAndInheritedFields.get(1).getName());
        assertEquals("testBeanSampler", declaredAndInheritedFields.get(2).getName());
    }

    @Test
    void preparationAnnotationIsDetected() throws NoSuchFieldException {
        final Field field = TestBean.class.getDeclaredField("testBeanSampler");
        assertTrue(JUnitSamplerUtils.shouldBeSampled(field));
    }

    @Test
    void aSamplerCanBeCreatedAndAssignedToAField() throws NoSuchFieldException {
        // WITH
        final Field field = TestBean.class.getDeclaredField("testBeanSampler");
        final TestBean bean = new TestBean();

        // DO
        JUnitSamplerUtils.assignNewSamplerToField(bean, field);

        // THEN EXPECT
        assertNotNull(bean.testBeanSampler);
    }

    @Test
    void samplerFixtureFromTestMethodCanBeLoaded() throws NoSuchMethodException {
        // WHEN
        final Optional<SamplerFixture> loadedFixture = JUnitSamplerUtils.loadSamplerFixtureFromMethodOrDeclaringClass(Example.class.getMethod("useSamplerFixture"));

        // THEN
        assertTrue(loadedFixture.isPresent());
    }

    @Test
    void samplerFixtureWithoutDefaultConstructorCannotBeLoaded() throws NoSuchMethodException {
        // WHEN
        final Method brokenMethod = Example.class.getMethod("useBrokenSamplerFixture");
        final JUnitPreparationException expectedException = assertThrows(JUnitPreparationException.class, () -> JUnitSamplerUtils.loadSamplerFixtureFromMethodOrDeclaringClass(brokenMethod));

        // THEN
        assertEquals("The SamplerFixture de.ppi.deepsampler.junit.JUnitSamplerUtilsTest$SamplerFixtureWithoutDefaultConstructor " +
                "must provide a default constructor.", expectedException.getMessage());
    }



    private int sortByName(final Field left, final Field right) {
        return left.getName().compareTo(right.getName());
    }

    private static class TestBean {

        @PrepareSampler
        private TestBean testBeanSampler;

        private final String aRandomStringField = "SomeRandomString";
    }

    private static class SubTestBean extends JUnitSamplerUtilsTest.TestBean {
        private final int aRandomIntField = 42;
    }

    private static class Example {

        @UseSamplerFixture(ExampleSamplerFixture.class)
        public void useSamplerFixture() {
            // nothing to do here because we only want to test loading the SamplerFixture.
        }

        @UseSamplerFixture(SamplerFixtureWithoutDefaultConstructor.class)
        public void useBrokenSamplerFixture() {
            // nothing to do here, we just want to test, that an Exception is thrown because this SamplerFixture doesn't have a
            // default constructor.

        }
    }

    public static class ExampleSamplerFixture implements SamplerFixture {

        @Override
        public void defineSamplers() {
            // Nothing to do here, because we only want to test if a SamplerFixture can be found and loaded
        }
    }

    private static class SamplerFixtureWithoutDefaultConstructor implements SamplerFixture {

        public SamplerFixtureWithoutDefaultConstructor(final String someParameter) {
            // nothing to do here, we just want to test, that an Exception is thrown because this SamplerFixture doesn't have a
            // default constructor
        }


        @Override
        public void defineSamplers() {
            // nothing to do here because we expect an error because of the missing default constructor.
        }
    }

}