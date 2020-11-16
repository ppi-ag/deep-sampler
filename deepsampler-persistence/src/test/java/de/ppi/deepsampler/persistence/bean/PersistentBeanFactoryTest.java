package de.ppi.deepsampler.persistence.bean;

import de.ppi.deepsampler.persistence.bean.ext.StandardBeanFactoryExtension;
import de.ppi.deepsampler.persistence.model.PersistentBean;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PersistentBeanFactoryTest {

    @Test
    void ofBeanSimple() {
        // GIVEN
        final String keyOne = "0$abc";
        final String keyTwo = "0$def";
        final Map<String, Object> values = new HashMap<>();
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        final SimpleTestBean testBean = new PersistentBeanFactory().createValueFromPersistentBean(defaultPersistentBean, SimpleTestBean.class);

        // THEN
        assertEquals("ME AND ALL", testBean.abc);
        assertEquals("ME AND MORE", testBean.def);
    }

    @Test
    void ofBeanWithSuperclass() {
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
        final TestBeanWithSuperclass testBean = new PersistentBeanFactory().createValueFromPersistentBean(defaultPersistentBean, TestBeanWithSuperclass.class);

        // THEN
        assertEquals("ME AND ALL in SUPERCLASS", testBean.getAbcSuperClass());
        assertEquals("ME AND MORE in SUPERCLASS", testBean.def);
        assertEquals("ME AND ALL", testBean.abc);
        assertEquals("ME AND MORE", testBean.yxc);
    }

    @Test
    void testOfBeanArray() {
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
        final SimpleTestBean[] testBean = new PersistentBeanFactory().ofBean(new DefaultPersistentBean[] {defaultPersistentBean, defaultPersistentBean2}, SimpleTestBean.class);

        // THEN
        assertEquals("ME AND ALL", testBean[0].abc);
        assertEquals("ME AND MORE", testBean[0].def);
        assertEquals("ME AND ALL2", testBean[1].abc);
        assertEquals("ME AND MORE2", testBean[1].def);
    }

    @Test
    void testOfBeanInBean() {
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
        final SimpleTestBeanRec testBean = new PersistentBeanFactory().createValueFromPersistentBean(defaultPersistentBean, SimpleTestBeanRec.class);

        // THEN
        assertEquals("ME AND MORE", testBean.str);
        assertEquals("a string", testBean.beanInBean.str);
        assertNull(testBean.beanInBean.beanInBean);
    }

    @Test
    void testOfBeanPrimitive() {
        // GIVEN
        final Map<String, Object> values = new HashMap<>();
        values.put("0$simpleInt", 2);
        values.put("0$longArray", new long[] {12, 32, 45});
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        final SimpleTestBeanWithPrimitive testBean = new PersistentBeanFactory().createValueFromPersistentBean(defaultPersistentBean, SimpleTestBeanWithPrimitive.class);

        // THEN
        assertEquals(2, testBean.simpleInt);
        assertArrayEquals(new long[] {12, 32, 45}, testBean.longArray);
    }

    @Test
    void ofBeanWithDates() {
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
        final SimpleTestBeanWithDates testBean = new PersistentBeanFactory().createValueFromPersistentBean(defaultPersistentBean, SimpleTestBeanWithDates.class);

        // THEN
        assertEquals(today, testBean.localDate);
        assertEquals(now, testBean.localDateTime);
        assertEquals(nowSqlDate, testBean.sqlDate);
        assertEquals(nowUtilDate, testBean.utilDate);
    }

    @Test
    void toBean() {
        // GIVEN
        final SimpleTestBean testBean = new SimpleTestBean();
        testBean.abc = "123";
        testBean.def = "456";

        // WHEN
        final PersistentBean bean  = new PersistentBeanFactory().toBean(testBean);

        // THEN
        assertEquals("123", bean.getValue("0$abc"));
        assertEquals("456", bean.getValue("0$def"));
    }

    @Test
    void toBeanRecursive() {
        // GIVEN
        final SimpleTestBeanRec testBean = new SimpleTestBeanRec();
        testBean.beanInBean = new SimpleTestBeanRec();
        testBean.beanInBean.str = "REC";
        testBean.str = "ABC";

        // WHEN
        final PersistentBean bean  = new PersistentBeanFactory().toBean(testBean);

        // THEN
        assertEquals("ABC", bean.getValue("0$str"));
        assertEquals("REC",((PersistentBean) bean.getValue("0$beanInBean")).getValue("0$str"));
    }

    @Test
    void toBeanPrimitive() {
        // GIVEN
        final SimpleTestBeanWithPrimitive testBean = new SimpleTestBeanWithPrimitive();
        testBean.simpleInt = 2;
        testBean.longArray = new long[] {21};

        // WHEN
        final PersistentBean bean  = new PersistentBeanFactory().toBean(testBean);

        // THEN
        assertEquals(2, bean.getValue("0$simpleInt"));
        assertArrayEquals(new long[] {21}, (long[]) bean.getValue("0$longArray"));
    }

    @Test
    void toBeanWithSuperclass() {
        // GIVEN
        final TestBeanWithSuperclass testBean = new TestBeanWithSuperclass();
        testBean.abc = "abc";
        testBean.def = "def";
        testBean.yxc = "yxc";
        testBean.setAbcSuperClass("SUPER");

        // WHEN
        final PersistentBean bean  = new PersistentBeanFactory().toBean(testBean);

        // THEN
        assertEquals("abc", bean.getValue("0$abc"));
        assertEquals("SUPER", bean.getValue("1$abc"));
        assertEquals("def", bean.getValue("1$def"));
        assertEquals("yxc", bean.getValue("0$yxc"));
    }

    @Test
    void toBeanWithSimpleExtension() {
        // GIVEN
        SimpleTestBean simpleTestBean = new SimpleTestBean();
        simpleTestBean.abc = "A";
        PersistentBeanFactory persistentBeanFactory = new PersistentBeanFactory();
        persistentBeanFactory.addExtension(new SimpleTestExtension());
        SimpleTestBeanWithPrimitive simpleTestBeanWithPrimitive = new SimpleTestBeanWithPrimitive();
        simpleTestBeanWithPrimitive.simpleInt = 3;

        // WHEN
        PersistentBean bean = persistentBeanFactory.toBean(simpleTestBean);
        PersistentBean beanWithPrim = persistentBeanFactory.toBean(simpleTestBeanWithPrimitive);

        // THEN
        assertEquals(0, bean.getValues().size());
        assertEquals(2, beanWithPrim.getValues().size());
        assertEquals(3, beanWithPrim.getValue("0$simpleInt"));
    }

    @Test
    void ofBeanWithSimpleExtension() {
        // GIVEN
        final String keyOne = "0$abc";
        final String keyTwo = "0$def";
        final Map<String, Object> values = new HashMap<>();
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);
        PersistentBeanFactory persistentBeanFactory = new PersistentBeanFactory();
        persistentBeanFactory.addExtension(new SimpleTestExtension());

        // WHEN
        final SimpleTestBean testBean = persistentBeanFactory.createValueFromPersistentBean(defaultPersistentBean, SimpleTestBean.class);

        // THEN
        assertNull(testBean.abc);
        assertNull(testBean.def);
    }

    private static class SimpleTestExtension extends StandardBeanFactoryExtension {

        @Override
        public boolean isProcessable(Class<?> beanCls) {
            return SimpleTestBean.class.isAssignableFrom(beanCls);
        }

        @Override
        public PersistentBean toBean(Object bean) {
            return new DefaultPersistentBean();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T ofBean(PersistentBean bean, Class<T> cls) {
            return (T) new SimpleTestBean();
        }
    }

    @Test
    void ofBeanSimpleImmutable() {
        // GIVEN
        final String keyOne = "0$abc";
        final String keyTwo = "0$def";
        final Map<String, Object> values = new LinkedHashMap<>();
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        final DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        final ImmutableSimpleTestBean testBean = new PersistentBeanFactory()
                .createValueFromPersistentBean(defaultPersistentBean, ImmutableSimpleTestBean.class);

        // THEN
        assertEquals("ME AND ALL", testBean.getAbc());
        assertEquals("ME AND MORE", testBean.getDef());
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