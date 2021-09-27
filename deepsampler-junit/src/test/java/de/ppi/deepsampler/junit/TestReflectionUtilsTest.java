/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TestReflectionUtilsTest {

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



    private int sortByName(final Field left, final Field right) {
        return left.getName().compareTo(right.getName());
    }

    @SuppressWarnings("unused")
    private static class TestBean {

        @PrepareSampler
        private TestBean testBeanSampler;

        private final String aRandomStringField = "SomeRandomString";
    }

    @SuppressWarnings("unused")
    private static class SubTestBean extends TestReflectionUtilsTest.TestBean {
        private final int aRandomIntField = 42;
    }

}