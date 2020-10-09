package org.deepsampler.persistence.json.bean;

import org.deepsampler.persistence.json.model.PersistentBean;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PersistentBeanFactoryTest {

    @Test
    void ofBeanSimple() {
        // GIVEN
        String keyOne = "0$abc";
        String keyTwo = "0$def";
        Map<String, Object> values = new HashMap<>();
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        SimpleTestBean testBean = PersistentBeanFactory.ofBean(defaultPersistentBean, SimpleTestBean.class);

        // THEN
        assertEquals("ME AND ALL", testBean.abc);
        assertEquals("ME AND MORE", testBean.def);
    }

    @Test
    void ofBeanWithSuperclass() {
        // GIVEN
        String keySuperclassOne = "1$abc";
        String keySuperclassTwo = "1$def";
        String keyOne = "0$abc";
        String keyTwo = "0$yxc";
        Map<String, Object> values = new HashMap<>();
        values.put(keySuperclassOne, "ME AND ALL in SUPERCLASS");
        values.put(keySuperclassTwo, "ME AND MORE in SUPERCLASS");
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        TestBeanWithSuperclass testBean = PersistentBeanFactory.ofBean(defaultPersistentBean, TestBeanWithSuperclass.class);

        // THEN
        assertEquals("ME AND ALL in SUPERCLASS", testBean.getAbcSuperClass());
        assertEquals("ME AND MORE in SUPERCLASS", testBean.def);
        assertEquals("ME AND ALL", testBean.abc);
        assertEquals("ME AND MORE", testBean.yxc);
    }

    @Test
    void testOfBeanArray() {
        // GIVEN
        String keyOne = "0$abc";
        String keyTwo = "0$def";
        Map<String, Object> values = new HashMap<>();
        values.put(keyOne, "ME AND ALL");
        values.put(keyTwo, "ME AND MORE");
        Map<String, Object> values2 = new HashMap<>();
        values2.put(keyOne, "ME AND ALL2");
        values2.put(keyTwo, "ME AND MORE2");
        DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);
        DefaultPersistentBean defaultPersistentBean2 = new DefaultPersistentBean(values2);

        // WHEN
        SimpleTestBean[] testBean = PersistentBeanFactory.ofBean(new DefaultPersistentBean[] {defaultPersistentBean, defaultPersistentBean2}, SimpleTestBean.class);

        // THEN
        assertEquals("ME AND ALL", testBean[0].abc);
        assertEquals("ME AND MORE", testBean[0].def);
        assertEquals("ME AND ALL2", testBean[1].abc);
        assertEquals("ME AND MORE2", testBean[1].def);
    }

    @Test
    void testOfBeanInBean() {
        // GIVEN
        Map<String, Object> values = new HashMap<>();
        values.put("0$str", "a string");
        DefaultPersistentBean defaultPersistentBeanInBean = new DefaultPersistentBean(values);
        String keyOne = "0$beanInBean";
        String keyTwo = "0$str";
        Map<String, Object> values2 = new HashMap<>();
        values2.put(keyOne, defaultPersistentBeanInBean);
        values2.put(keyTwo, "ME AND MORE");
        DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values2);

        // WHEN
        SimpleTestBeanRec testBean = PersistentBeanFactory.ofBean(defaultPersistentBean, SimpleTestBeanRec.class);

        // THEN
        assertEquals("ME AND MORE", testBean.str);
        assertEquals("a string", testBean.beanInBean.str);
        assertNull(testBean.beanInBean.beanInBean);
    }

    @Test
    void testOfBeanPrimitive() {
        // GIVEN
        Map<String, Object> values = new HashMap<>();
        values.put("0$simpleInt", 2);
        values.put("0$longArray", new long[] {12, 32, 45});
        DefaultPersistentBean defaultPersistentBean = new DefaultPersistentBean(values);

        // WHEN
        SimpleTestBeanWithPrimitive testBean = PersistentBeanFactory.ofBean(defaultPersistentBean, SimpleTestBeanWithPrimitive.class);

        // THEN
        assertEquals(2, testBean.simpleInt);
        assertArrayEquals(new long[] {12, 32, 45}, testBean.longArray);
    }

    @Test
    void toBean() {
        // GIVEN
        SimpleTestBean testBean = new SimpleTestBean();
        testBean.abc = "123";
        testBean.def = "456";

        // WHEN
        PersistentBean bean  = PersistentBeanFactory.toBean(testBean);

        // THEN
        assertEquals("123", bean.getValue("0$abc"));
        assertEquals("456", bean.getValue("0$def"));
    }

    @Test
    void toBeanRecursive() {
        // GIVEN
        SimpleTestBeanRec testBean = new SimpleTestBeanRec();
        testBean.beanInBean = new SimpleTestBeanRec();
        testBean.beanInBean.str = "REC";
        testBean.str = "ABC";

        // WHEN
        PersistentBean bean  = PersistentBeanFactory.toBean(testBean);

        // THEN
        assertEquals("ABC", bean.getValue("0$str"));
        assertEquals("REC",((PersistentBean) bean.getValue("0$beanInBean")).getValue("0$str"));
    }

    @Test
    void toBeanPrimitive() {
        // GIVEN
        SimpleTestBeanWithPrimitive testBean = new SimpleTestBeanWithPrimitive();
        testBean.simpleInt = 2;
        testBean.longArray = new long[] {21};

        // WHEN
        PersistentBean bean  = PersistentBeanFactory.toBean(testBean);

        // THEN
        assertEquals(2, bean.getValue("0$simpleInt"));
        assertArrayEquals(new long[] {21}, (long[]) bean.getValue("0$longArray"));
    }

    @Test
    void toBeanWithSuperclass() {
        // GIVEN
        TestBeanWithSuperclass testBean = new TestBeanWithSuperclass();
        testBean.abc = "abc";
        testBean.def = "def";
        testBean.yxc = "yxc";
        testBean.setAbcSuperClass("SUPER");

        // WHEN
        PersistentBean bean  = PersistentBeanFactory.toBean(testBean);

        // THEN
        assertEquals("abc", bean.getValue("0$abc"));
        assertEquals("SUPER", bean.getValue("1$abc"));
        assertEquals("def", bean.getValue("1$def"));
        assertEquals("yxc", bean.getValue("0$yxc"));
    }

    private static class SimpleTestBean {
        protected String abc;
        protected String def;
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

        public void setAbcSuperClass(String abc) {
            super.abc = abc;
        }
    }
}