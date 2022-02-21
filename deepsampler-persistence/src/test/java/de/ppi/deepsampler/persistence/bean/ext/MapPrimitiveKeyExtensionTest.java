/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MapPrimitiveKeyExtensionTest {

    public static final String KEY = "key";

    @Test
    void isProcessableShouldAcceptOnlyPrimitiveKeyMaps() throws NoSuchMethodException {
        // GIVEN
        MapPrimitiveKeyExtension extension = new MapPrimitiveKeyExtension();

        Method method = TestService.class.getMethod("getStringMap");
        ParameterizedType stringMapReturnType = (ParameterizedType) method.getGenericReturnType();

        method = TestService.class.getMethod("getDateMap");
        ParameterizedType dateMapReturnType = (ParameterizedType) method.getGenericReturnType();

        method = TestService.class.getMethod("getDateKeyMap");
        ParameterizedType dateKeyMapReturnType = (ParameterizedType) method.getGenericReturnType();

        method = TestService.class.getMethod("getDateKeyMap");
        ParameterizedType stringListType = (ParameterizedType) method.getGenericReturnType();

        // THEN
        assertTrue(extension.isProcessable(HashMap.class, stringMapReturnType));
        assertTrue(extension.isProcessable(HashMap.class, dateMapReturnType));
        assertFalse(extension.isProcessable(HashMap.class, dateKeyMapReturnType));

        assertTrue(extension.isProcessable(Collections.unmodifiableMap(new HashMap<>()).getClass(), stringMapReturnType));
        assertTrue(extension.isProcessable(HashMap.class, stringMapReturnType));

        assertFalse(extension.isProcessable(String.class, null));
        assertFalse(extension.isProcessable(ArrayList.class, stringListType));
        assertFalse(extension.isProcessable(Date.class, null));
    }


    @Test
    void convertShouldAcceptOnlyMaps() throws NoSuchMethodException {
        // GIVEN
        MapPrimitiveKeyExtension extension = new MapPrimitiveKeyExtension();
        Date now = new Date();
        Map<String, String> map = new HashMap<>();

        Method method = TestService.class.getMethod("getStringMap");
        ParameterizedType stringMapReturnType = (ParameterizedType) method.getGenericReturnType();

        // THEN
        assertThrows(PersistenceException.class, () -> extension.convert(now, null, null));
        assertDoesNotThrow(() -> extension.convert(map, stringMapReturnType, null));
    }

    @Test
    void shouldConvertStringHashMap() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new MapPrimitiveKeyExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getDateMap");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        Map<String, Date> map = new HashMap<>();
        map.put(KEY, new Date());

        Map<String, Date> result = converter.convert(map, returnType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(KEY));
    }

    @Test
    void shouldConvertIntegerHashMap() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new MapPrimitiveKeyExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getIntegerMap");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);

        Map<String, Integer> result = converter.convert(map, returnType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get("1"));
    }

    @Test
    void shouldConvertUnmodifiableMapAndReplaceItByHasMap() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new MapPrimitiveKeyExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getUnmodifiableMap");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        Map<String, Date> map = new HashMap<>();
        map.put(KEY, new Date());
        Map<String, Date> unmodifiableMap = Collections.unmodifiableMap(map);

        // WHEN
        Map<String, Date> result = converter.convert(unmodifiableMap, returnType);

        // THEN
        assertNotNull(result);
        assertTrue(result instanceof HashMap);
        assertFalse(unmodifiableMap instanceof HashMap);
        assertEquals(1, result.size());
        assertNotNull(result.get(KEY));
    }

    @Test
    void shouldConvertRecursiveHasMap() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new MapPrimitiveKeyExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getRecursiveDateMap");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        Map<String, Map<String, Date>> map = new TestService().getRecursiveDateMap();

        Map<String, Map<String, Date>> result = converter.convert(map, returnType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(KEY));
        assertEquals(1, result.get(KEY).size());
        assertNotNull(result.get(KEY).get(KEY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRevertStringHashMap() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new MapPrimitiveKeyExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getDateMap");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        Map<String, Date> map = new HashMap<>();
        map.put(KEY, new Date());

        Map<String, Date> result = converter.convert(map, returnType);

        // WHEN
        Map<String, Date> reverted = converter.revert(result, HashMap.class, returnType);
        // THEN

        assertNotNull(reverted);
        assertEquals(1, reverted.size());
        assertEquals(map.get(KEY).getTime(), reverted.get(KEY).getTime());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRevertIntegerHashMap() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new MapPrimitiveKeyExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getIntegerMap");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 2);

        Map<String, Integer> result = converter.convert(map, returnType);

        // WHEN
        Map<Integer, Integer> reverted = converter.revert(result, HashMap.class, returnType);
        // THEN

        assertNotNull(reverted);
        assertEquals(1, reverted.size());
        assertEquals(map.get(1), reverted.get(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRevertRecursiveMap() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new MapPrimitiveKeyExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getRecursiveDateMap");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        Map<String, Map<String, Date>> map = new TestService().getRecursiveDateMap();

        Map<String, Map<String, Date>> result = converter.convert(map, returnType);

        // WHEN
        Map<String, Map<String, Date>> reverted = converter.revert(result, HashMap.class, returnType);

        // THEN
        assertNotNull(reverted);
        assertEquals(1, reverted.size());
        assertEquals(1, reverted.get(KEY).size());
        assertEquals(map.get(KEY).get(KEY).getTime(), reverted.get(KEY).get(KEY).getTime());
    }



    private static class TestService {

        public Map<String, String> getStringMap() {
            return new HashMap<>();
        }

        public Map<String, Date> getDateMap() {
            return new HashMap<>();
        }

        public Map<Date, Date> getDateKeyMap() {
            return new HashMap<>();
        }

        public Map<String, Date> getUnmodifiableMap() {
            return Collections.unmodifiableMap(new HashMap<>());
        }

        public Map<Integer, Integer> getIntegerMap() {
            return new HashMap<>();
        }

        public Map<String, Map<String, Date>> getRecursiveDateMap() {
            Map<String, Map<String, Date>> outerMap =  new HashMap<>();
            Map<String, Date> innerMap =  new HashMap<>();

            innerMap.put(KEY, new Date());
            outerMap.put(KEY, innerMap);

            return outerMap;
        }
    }

    private static class WrongMap<T> extends HashMap<T, String> {

    }
}