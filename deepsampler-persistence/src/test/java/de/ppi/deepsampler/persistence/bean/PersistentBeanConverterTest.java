/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean;

import de.ppi.deepsampler.persistence.bean.ext.StandardBeanConverterExtension;
import de.ppi.deepsampler.persistence.model.PersistentBean;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PersistentBeanConverterTest {

    @Test
    void simpleBeanShouldBeReverted() {
        // GIVEN
        final String keyOne = "0$abc";
        final String keyTwo = "0$def";
        final Map<String, Object> values = new HashMap<>();
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        final SimpleTestBean testBean = new PersistentBeanConverter().revert(defaultPersistentBean, SimpleTestBean.class, null);

        // THEN
        assertEquals("ME AND ALL", testBean.abc);
        assertEquals("ME AND MORE", testBean.def);
    }

    @Test
    void beanWithSuperclassShouldBeReverted() {
        // GIVEN
        final String keySuperclassOne = "1$abc";
        final String keySuperclassTwo = "1$def";
        final String keyOne = "0$abc";
        final String keyTwo = "0$yxc";
        final Map<String, Object> values = new HashMap<>();
        values.put(keySuperclassOne, "ME AND ALL in SUPERCLASS");
        values.put(keySuperclassTwo, "ME AND MORE in SUPERCLASS");
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        final TestBeanWithSuperclass testBean = new PersistentBeanConverter().revert(defaultPersistentBean, TestBeanWithSuperclass.class, null);

        // THEN
        assertEquals("ME AND ALL in SUPERCLASS", testBean.getAbcSuperClass());
        assertEquals("ME AND MORE in SUPERCLASS", testBean.def);
        assertEquals("ME AND ALL", testBean.abc);
        assertEquals("ME AND MORE", testBean.yxc);
    }

    @Test
    void beanArrayShouldBe() {
        // GIVEN
        final String keyOne = "0$abc";
        final String keyTwo = "0$def";
        final Map<String, Object> values = new HashMap<>();
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        final Map<String, Object> values2 = new HashMap<>();
        values2.put(keyOne, "ME AND ALL2");
        values2.put(keyTwo, "ME AND MORE2");
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);
        final DefaultPersistentBean defaultPersistentBean2 = new DefaultPersistentBean(values2);

        // WHEN
        final SimpleTestBean[] testBean = new PersistentBeanConverter().revert(new DefaultPersistentBean[] {defaultPersistentBean, defaultPersistentBean2}, SimpleTestBean[].class, null);

        // THEN
        assertEquals("ME AND ALL", testBean[0].abc);
        assertEquals("ME AND MORE", testBean[0].def);
        assertEquals("ME AND ALL2", testBean[1].abc);
        assertEquals("ME AND MORE2", testBean[1].def);
    }

    @Test
    void beanInBeanShouldBeReverted() {
        // GIVEN
        final Map<String, Object> values = new HashMap<>();
        values.put("0$str", "a string");
        final DefaultPersistentBean defaultPersistentBeanInBean = new DefaultPersistentBean(values);
        final String keyOne = "0$beanInBean";
        final String keyTwo = "0$str";
        final Map<String, Object> values2 = new HashMap<>();
        values2.put(keyOne, defaultPersistentBeanInBean);
        values2.put(keyTwo, "ME AND MORE");
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values2);

        // WHEN
        final SimpleTestBeanRec testBean = new PersistentBeanConverter().revert(defaultPersistentBean, SimpleTestBeanRec.class, null);

        // THEN
        assertEquals("ME AND MORE", testBean.str);
        assertEquals("a string", testBean.beanInBean.str);
        assertNull(testBean.beanInBean.beanInBean);
    }

    @Test
    void primitiveBeanShouldBereverted() {
        // GIVEN
        final Map<String, Object> values = new HashMap<>();
        values.put("0$simpleInt", 2);
        values.put("0$longArray", new long[] {12, 32, 45});
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        final SimpleTestBeanWithPrimitive testBean = new PersistentBeanConverter().revert(defaultPersistentBean, SimpleTestBeanWithPrimitive.class, null);

        // THEN
        assertEquals(2, testBean.simpleInt);
        assertArrayEquals(new long[] {12, 32, 45}, testBean.longArray);
    }

    @Test
    void beanWithDatesShouldBeReverted() {
        // GIVEN
        final String localDate = "0$localDate";
        final String localDateTime = "0$localDateTime";
        final String utilDate = "0$utilDate";
        final String sqlDate = "0$sqlDate";

        final Map<String, Object> values = new HashMap<>();

        final LocalDate today = LocalDate.now();
        final LocalDateTime now = LocalDateTime.now();
        final Date nowUtilDate = new Date();
        final java.sql.Date nowSqlDate = java.sql.Date.valueOf(today);

        values.put(localDate, today);
        values.put(localDateTime, now);
        values.put(utilDate, nowUtilDate);
        values.put(sqlDate, nowSqlDate);

        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        final SimpleTestBeanWithDates testBean = new PersistentBeanConverter().revert(defaultPersistentBean, SimpleTestBeanWithDates.class, null);

        // THEN
        assertEquals(today, testBean.localDate);
        assertEquals(now, testBean.localDateTime);
        assertEquals(nowSqlDate, testBean.sqlDate);
        assertEquals(nowUtilDate, testBean.utilDate);
    }

    @Test
    void beanShouldBeConverted() {
        // GIVEN
        final SimpleTestBean testBean = new SimpleTestBean();
        testBean.abc = "123";
        testBean.def = "456";

        // WHEN
        final PersistentBean bean  = new PersistentBeanConverter().convert(testBean, null);

        // THEN
        assertEquals("123", bean.getValue("0$abc"));
        assertEquals("456", bean.getValue("0$def"));
    }

    @Test
    void recursiveBeanShouldBeReverted() {
        // GIVEN
        final SimpleTestBeanRec testBean = new SimpleTestBeanRec();
        testBean.beanInBean = new SimpleTestBeanRec();
        testBean.beanInBean.str = "REC";
        testBean.str = "ABC";

        // WHEN
        final PersistentBean bean  = new PersistentBeanConverter().convert(testBean, null);
        SimpleTestBeanRec[] beanRec = new PersistentBeanConverter().revert(new PersistentBean[] {bean}, SimpleTestBeanRec[].class, null);

        // THEN
        assertEquals(testBean.beanInBean.str, beanRec[0].beanInBean.str);
        assertEquals("ABC", bean.getValue("0$str"));
        assertEquals("REC",((PersistentBean) bean.getValue("0$beanInBean")).getValue("0$str"));
    }

    @Test
    void primitiveBeanShouldBeConverted() {
        // GIVEN
        final SimpleTestBeanWithPrimitive testBean = new SimpleTestBeanWithPrimitive();
        testBean.simpleInt = 2;
        testBean.longArray = new long[] {21};

        // WHEN
        final PersistentBean bean  = new PersistentBeanConverter().convert(testBean, null);

        // THEN
        assertEquals(2, bean.getValue("0$simpleInt"));
        assertArrayEquals(new long[] {21}, (long[]) bean.getValue("0$longArray"));
    }

    @Test
    void beanWithSuperclassShouldBeConverted() {
        // GIVEN
        final TestBeanWithSuperclass testBean = new TestBeanWithSuperclass();
        testBean.abc = "abc";
        testBean.def = "def";
        testBean.yxc = "yxc";
        testBean.setAbcSuperClass("SUPER");

        // WHEN
        final PersistentBean bean  = new PersistentBeanConverter().convert(testBean, null);

        // THEN
        assertEquals("abc", bean.getValue("0$abc"));
        assertEquals("SUPER", bean.getValue("1$abc"));
        assertEquals("def", bean.getValue("1$def"));
        assertEquals("yxc", bean.getValue("0$yxc"));
    }

    @Test
    void beanWithSimpleExtensionShouldBeConverted() {
        // GIVEN
        SimpleTestBean simpleTestBean = new SimpleTestBean();
        simpleTestBean.abc = "A";
        PersistentBeanConverter persistentBeanConverter = new PersistentBeanConverter();
        persistentBeanConverter.addExtension(new SimpleTestExtension());
        SimpleTestBeanWithPrimitive simpleTestBeanWithPrimitive = new SimpleTestBeanWithPrimitive();
        simpleTestBeanWithPrimitive.simpleInt = 3;

        // WHEN
        PersistentBean bean = persistentBeanConverter.convert(simpleTestBean, null);
        PersistentBean beanWithPrim = persistentBeanConverter.convert(simpleTestBeanWithPrimitive, null);

        // THEN
        assertEquals(0, bean.getValues().size());
        assertEquals(2, beanWithPrim.getValues().size());
        assertEquals(3, beanWithPrim.getValue("0$simpleInt"));
    }

    @Test
    void beanWithSimpleExtensionShouldBeReverted() {
        // GIVEN
        final String keyOne = "0$abc";
        final String keyTwo = "0$def";
        final Map<String, Object> values = new HashMap<>();
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);
        PersistentBeanConverter persistentBeanConverter = new PersistentBeanConverter();
        persistentBeanConverter.addExtension(new SimpleTestExtension());

        // WHEN
        final SimpleTestBean testBean = persistentBeanConverter.revert(defaultPersistentBean, SimpleTestBean.class, null);

        // THEN
        assertNull(testBean.abc);
        assertNull(testBean.def);
    }

    private static class SimpleTestExtension extends StandardBeanConverterExtension {

        @Override
        public boolean isProcessable(Class<?> beanClass, ParameterizedType beanType) {
            return SimpleTestBean.class.isAssignableFrom(beanClass);
        }

        @Override
        public PersistentBean convert(Object originalBean, ParameterizedType beanType, PersistentBeanConverter persistentBeanConverter) {
            return new DefaultPersistentBean();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T revert(Object bean, Class<T> targetClass, ParameterizedType targetType,  PersistentBeanConverter persistentBeanConverter) {
            return (T) new SimpleTestBean();
        }
    }

    @Test
    void simpleImmutableBeanShouldBeReverted() {
        // GIVEN
        final String keyOne = "0$abc";
        final String keyTwo = "0$def";
        final Map<String, Object> values = new LinkedHashMap<>();
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        final ImmutableSimpleTestBean testBean = new PersistentBeanConverter()
                .revert(defaultPersistentBean, ImmutableSimpleTestBean.class, null);

        // THEN
        assertEquals("ME AND ALL", testBean.getAbc());
        assertEquals("ME AND MORE", testBean.getDef());
    }

    @Test
    void testImmutableCollectionBean() {
        // GIVEN
        final CollectionBean bean = new CollectionBean();
        bean.collectionOfStrings = Arrays.asList("AB", "CD");

        // WHEN
        PersistentBean persistentBean = new PersistentBeanConverter().convert(bean, null);

        // THEN
        assertNotNull(persistentBean.getValue("0$collectionOfStrings"));
    }

    @Test
    void testConvertingAndReversingCharacter() {
        // GIVEN
        Cs cs = new Cs();
        cs.character = '2';
        PersistentBean b = new PersistentBeanConverter().convert(cs, null);

        // WHEN
        Cs[] result = new PersistentBeanConverter().revert(new PersistentBean[] {b}, Cs[].class, null);

        // THEN
        assertEquals(cs.character, result[0].character);
    }

    @Test
    void revertWithObjectContainerThatContainsAnArray() {
        // GIVEN
        TestBeanWithBeanArray testBeanContainer = new TestBeanWithBeanArray();
        SimpleTestBean[] testBeanArray = new SimpleTestBean[1];
        testBeanArray[0] = new SimpleTestBean();
        testBeanArray[0].abc = "make it so";
        testBeanContainer.testBeanArray = testBeanArray;

        // FROM
        PersistentBeanConverter converter = new PersistentBeanConverter();
        PersistentBean bean = converter.convert(testBeanContainer, null);

        // TO
        TestBeanWithBeanArray resultBeanRef = converter.revert(bean, TestBeanWithBeanArray.class, null);

        // THEN
        assertNotNull(resultBeanRef);
        assertNotNull(resultBeanRef.testBeanArray);
        assertEquals(1, resultBeanRef.testBeanArray.length);
        assertNotNull(resultBeanRef.testBeanArray[0]);
        assertEquals("make it so", resultBeanRef.testBeanArray[0].abc);
    }

    @Test
    void revertWithArray() {
        // GIVEN
        SimpleTestBean[] testBeanArray = new SimpleTestBean[1];
        testBeanArray[0] = new SimpleTestBean();
        testBeanArray[0].abc = "make it so";

        // FROM
        PersistentBeanConverter converter = new PersistentBeanConverter();
        PersistentBean[] persistentBean = converter.convert(testBeanArray, null);

        // TO
        SimpleTestBean[] resultBean = converter.revert(persistentBean, SimpleTestBean[].class, null);

        // THEN
        assertNotNull(resultBean);
        assertEquals(1, resultBean.length);
        assertNotNull(resultBean[0]);
        assertEquals("make it so", resultBean[0].abc);
    }



    private static class TestBeanWithBeanArray {
        protected SimpleTestBean[] testBeanArray;
    }

    private static class CollectionBean {
        Collection<String> collectionOfStrings;
    }

    private static class ImmutableSimpleTestBean {
        private final String abc;
        private final String def;

        public ImmutableSimpleTestBean(String abc, String def) {
            this.abc = abc;
            this.def = def;
        }

        public String getAbc() {
            return abc;
        }

        public String getDef() {
            return def;
        }
    }

    private static class SimpleTestBean {
        protected String abc;
        protected String def;
    }

    private static class SimpleTestBeanWithDates {
        protected LocalDate localDate;
        protected LocalDateTime localDateTime;
        protected Date utilDate;
        protected java.sql.Date sqlDate;
    }

    private static class SimpleTestBeanWithPrimitive {
        protected int simpleInt;
        protected long[] longArray;
    }

    private static class SimpleTestBeanRec {
        protected SimpleTestBeanRec beanInBean;
        protected String str;
    }

    private static class Cs {
        Character character = '2';
    }

    private static class TestBeanWithSuperclass extends SimpleTestBean {
        private String abc;
        private String yxc;

        public String getAbcSuperClass() {
            return super.abc;
        }

        public void setAbcSuperClass(final String abc) {
            super.abc = abc;
        }
    }
}