package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CollectionExtensionTest {

    @Test
    void isProcessableShouldAcceptOnlyCollections() {
        // GIVEN
        CollectionExtension extension = new CollectionExtension();

        // THEN
        assertTrue(extension.isProcessable(Arrays.asList("", "").getClass(), null));
        assertTrue(extension.isProcessable(Collections.unmodifiableCollection(Arrays.asList("", "")).getClass(), null));
        assertTrue(extension.isProcessable(HashSet.class, null));
        assertTrue(extension.isProcessable(ArrayList.class, null));

        assertFalse(extension.isProcessable(String.class, null));
        assertFalse(extension.isProcessable(HashMap.class, null));
        assertFalse(extension.isProcessable(Date.class, null));
    }

    @Test
    void skipShouldSkipPrimitiveCollections() throws NoSuchMethodException {
        // GIVEN
        CollectionExtension extension = new CollectionExtension();

        Method method = TestService.class.getMethod("getList");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        Class<?> returnClass = method.getReturnType();

        // THEN
        assertTrue(extension.skip(returnClass, returnType));
    }

    @Test
    void skipShouldNotSkipNonPrimitiveCollections() throws NoSuchMethodException {
        // GIVEN
        CollectionExtension extension = new CollectionExtension();

        Method method = TestService.class.getMethod("getDateList");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        Class<?> returnClass = method.getReturnType();

        // THEN
        assertFalse(extension.skip(returnClass, returnType));
    }

    @Test
    void skipIsUnableToUseParameterizedClassWithoutActualParameterTypes() {
        // GIVEN
        CollectionExtension extension = new CollectionExtension();

        List<String> list = new ArrayList<>();
        list.add("");
        Class<?> listClass = list.getClass();
        ParameterizedType listType = (ParameterizedType) listClass.getGenericSuperclass();

        assertThrows(PersistenceException.class, () -> extension.skip(listClass, listType));
    }

    @Test
    void convertShouldAcceptOnlyCollections() {
        // GIVEN
        CollectionExtension extension = new CollectionExtension();
        Date now = new Date();

        // THEN
        assertThrows(PersistenceException.class, () -> extension.convert(now, null, null));
    }

    @Test
    void shouldConvertArrayList() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new CollectionExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getDateList");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        List<Date> list = new ArrayList<>();
        list.add(new Date());

        List<Date> result = converter.convert(list, returnType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    void shouldConvertLinkedListAndReturnLinkedList() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new CollectionExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getDateList");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        List<Date> list = new LinkedList<>();
        list.add(new Date());

        List<Date> result = converter.convert(list, returnType);
        assertNotNull(result);
        assertTrue(result instanceof LinkedList);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    void shouldConvertUnmodifiableListAndReplaceItByArrayList() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new CollectionExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getUnmodifiableList");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        List<Date> list = new ArrayList<>();
        list.add(new Date());
        List<Date> unmodifiableList = Collections.unmodifiableList(list);

        // WHEN
        List<Date> result = converter.convert(unmodifiableList, returnType);

        // TEHN
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
        assertFalse(unmodifiableList instanceof ArrayList);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    void shouldConvertUnmodifiableSetAndReplaceItByHashSet() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new CollectionExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getUnmodifiableSet");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        Set<Date> set = new HashSet<>();
        set.add(new Date());
        Set<Date> unmodifiableSet = Collections.unmodifiableSet(set);

        // WHEN
        Set<Date> result = converter.convert(unmodifiableSet, returnType);

        // TEHN
        assertNotNull(result);
        assertTrue(result instanceof HashSet);
        assertFalse(unmodifiableSet instanceof HashSet);
        assertEquals(1, result.size());
        assertNotNull(result.toArray()[0]);
    }

    @Test
    void shouldConvertRecursiveArrayList() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new CollectionExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getRecursiveList");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        List<List<Date>> list = new TestService().getRecursiveList();

        List<List<Date>> result = converter.convert(list, returnType);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
        assertEquals(1, result.get(0).size());
        assertNotNull(result.get(0).get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRevertArrayList() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new CollectionExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getDateList");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        List<Date> list = new ArrayList<>();
        list.add(new Date());

        List<Date> result = converter.convert(list, returnType);

        // WHEN
        List<Date> reverted = converter.revert(result, ArrayList.class, returnType);
        // THEN

        assertNotNull(reverted);
        assertEquals(1, reverted.size());
        assertEquals(list.get(0).getTime(), reverted.get(0).getTime());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRevertLinkedList() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new CollectionExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getDateList");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        List<Date> list = new LinkedList<>();
        list.add(new Date());

        List<Date> result = converter.convert(list, returnType);

        // WHEN
        List<Date> reverted = converter.revert(result, LinkedList.class, returnType);

        // THEN
        assertNotNull(reverted);
        assertEquals(1, reverted.size());
        assertEquals(list.get(0).getTime(), reverted.get(0).getTime());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRevertRecursiveList() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new CollectionExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getRecursiveList");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        List<List<Date>> list = new TestService().getRecursiveList();

        List<List<Date>> result = converter.convert(list, returnType);

        // WHEN
        List<List<Date>> reverted = converter.revert(result, ArrayList.class, returnType);

        // THEN
        assertNotNull(reverted);
        assertEquals(1, reverted.size());
        assertEquals(1, reverted.get(0).size());
        assertEquals(list.get(0).get(0).getTime(), reverted.get(0).get(0).getTime());
    }

    @Test
    void revertShouldNotAcceptTwoTypeParameters() throws NoSuchMethodException {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new CollectionExtension());
        converter.addExtension(new JavaTimeExtension());

        Method method = TestService.class.getMethod("getWrongList");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        WrongList<String, String> wrongList = new WrongList<>();

        List<Date> result = converter.convert(wrongList, returnType);

        // THEN
        assertThrows(PersistenceException.class, () -> converter.revert(result, WrongList.class, returnType));
    }



    private static class TestService {
        public List<String> getList() {
            return new ArrayList<>();
        }

        public List<Date> getDateList() {
            return new ArrayList<>();
        }

        public List<Date> getUnmodifiableList() {
            return Collections.unmodifiableList(new ArrayList<>());
        }

        public Set<Date> getUnmodifiableSet() {
            return Collections.unmodifiableSet(new HashSet<>());
        }

        public List<List<Date>> getRecursiveList() {
            List<Date> innerList = new ArrayList<>();
            innerList.add(new Date());

            List<List<Date>> outerList = new ArrayList<>();
            outerList.add(innerList);

            return outerList;
        }

        public WrongList<String, String> getWrongList() {
            return new WrongList<>();
        }

    }

    private static class WrongList<T, R> extends ArrayList<T> {
    }
}