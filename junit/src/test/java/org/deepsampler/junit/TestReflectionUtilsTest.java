package org.deepsampler.junit;

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
        Stream<Field> declaredAndInheritedFieldStream = TestReflectionUtils.getDeclaredAndInheritedFields(SubTestBean.class);

        // Since the order in which reflection iterates over fields is not defined and may change, we sort the list
        // so selecting expected Fields using indexes is save. We also need to filter some proxy-fields that are introduced by jacoco
        List<Field> declaredAndInheritedFields = declaredAndInheritedFieldStream.sorted(this::sortByName)//
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
        Field field = TestBean.class.getDeclaredField("testBeanSampler");
        assertTrue(TestReflectionUtils.shouldBeSampled(field));
    }

    @Test
    void aSamplerCanBeCreatedAndAssignedToAField() throws NoSuchFieldException {
        // WITH
        Field field = TestBean.class.getDeclaredField("testBeanSampler");
        TestBean bean = new TestBean();

        // DO
        TestReflectionUtils.assignNewSamplerToField(bean, field);

        // THEN EXPECT
        assertNotNull(bean.testBeanSampler);
    }



    private int sortByName(Field left, Field right) {
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