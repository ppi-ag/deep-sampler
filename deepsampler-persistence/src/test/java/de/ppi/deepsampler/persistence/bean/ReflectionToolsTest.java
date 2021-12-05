/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean;

import de.ppi.deepsampler.persistence.error.PersistenceException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionToolsTest {

    @Test
    void isObjectArrayShouldRejectAllPrimitives() {
        Integer[] integerArray = new Integer[0];
        int[] intArray = new int[0];
        Double[] doubles = new Double[0];
        double[] primitiveDoubles = new double[0];
        Float[] floats = new Float[0];
        float[] primitiveFloats = new float[0];
        Short[] shorts = new Short[0];
        short[] primitiveShorts = new short[0];
        Character[] chars = new Character[0];
        char[] primitiveChars = new char[0];
        String[] strings = new String[0];
        Byte[] bytes = new Byte[0];
        byte[] primitiveBytes = new byte[0];

        Date[] dates = new Date[0];

        assertFalse(ReflectionTools.isObjectArray(integerArray.getClass()));
        assertFalse(ReflectionTools.isObjectArray(intArray.getClass()));
        assertFalse(ReflectionTools.isObjectArray(doubles.getClass()));
        assertFalse(ReflectionTools.isObjectArray(primitiveDoubles.getClass()));
        assertFalse(ReflectionTools.isObjectArray(floats.getClass()));
        assertFalse(ReflectionTools.isObjectArray(primitiveFloats.getClass()));
        assertFalse(ReflectionTools.isObjectArray(shorts.getClass()));
        assertFalse(ReflectionTools.isObjectArray(primitiveShorts.getClass()));
        assertFalse(ReflectionTools.isObjectArray(chars.getClass()));
        assertFalse(ReflectionTools.isObjectArray(primitiveChars.getClass()));
        assertFalse(ReflectionTools.isObjectArray(strings.getClass()));
        assertFalse(ReflectionTools.isObjectArray(bytes.getClass()));
        assertFalse(ReflectionTools.isObjectArray(primitiveBytes.getClass()));

        assertTrue(ReflectionTools.isObjectArray(dates.getClass()));
    }

    @Test
    void isObjectArrayShouldRejectPrimitivesInMultidimensionalArray() {
        Integer[][][] integerArray = new Integer[0][0][0];

        assertFalse(ReflectionTools.isObjectArray(integerArray.getClass()));
    }

    @Test
    void primitivesAndWrapperShouldBeFound() {
        Integer someInteger = 1;
        int primitiveInt = 1;
        Double someDouble = 1.0;
        double primitiveDouble= 1.0;
        Float someFloat = 1.0f;
        float primitiveFloat = 1.0f;
        Short someShort = 1;
        short primitiveShort = ' ';
        Character someChar = ' ';
        char primitiveChar = ' ';
        String someString = "";
        Byte someByte = 1;
        byte primitiveByte = 1;

        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(someInteger)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(primitiveInt)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(someDouble)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(primitiveDouble)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(someFloat)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(primitiveFloat)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(someShort)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(primitiveShort)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(someChar)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(primitiveChar)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(someString)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(someByte)));
        assertTrue(ReflectionTools.isPrimitiveOrWrapper(toClass(primitiveByte)));

        assertFalse(ReflectionTools.isPrimitiveOrWrapper(Date.class));

    }

    @Test
    void primitiveWrapperInCollectionIsDetected() throws NoSuchMethodException {
        // GIVEN
        String notACollection = "";
        ArrayList<String> stringList = new ArrayList<>();
        Type stringCollection = getGenericReturnType("getStringCollection");
        Type collectionWithGenericType = getGenericReturnType("getCollectionOfCollections");

        // THEN
        assertFalse(ReflectionTools.hasPrimitiveTypeParameters(notACollection.getClass()));
        assertTrue(ReflectionTools.hasPrimitiveTypeParameters(stringCollection));
        assertFalse(ReflectionTools.hasPrimitiveTypeParameters(collectionWithGenericType));

        Type stringListType = stringList.getClass().getGenericSuperclass();
        assertThrows(PersistenceException.class, () -> ReflectionTools.hasPrimitiveTypeParameters(stringListType));
    }

    @Test
    void primitiveWrappersInMapAreDetected() throws NoSuchMethodException {
        // GIVEN
        String notAMap = "";
        HashMap<String, String> stringHashMap = new HashMap<>();
        Type stringMap = getGenericReturnType("getStringMap");
        Type mapWithGenericType = getGenericReturnType("getDateMap");

        // THEN
        assertFalse(ReflectionTools.hasPrimitiveTypeParameters(notAMap.getClass(), 2));
        assertTrue(ReflectionTools.hasPrimitiveTypeParameters(stringMap, 2));
        assertFalse(ReflectionTools.hasPrimitiveTypeParameters(mapWithGenericType, 2));

        Type hashMapType = stringHashMap.getClass().getGenericSuperclass();
        assertThrows(PersistenceException.class, () -> ReflectionTools.hasPrimitiveTypeParameters(hashMapType));

        assertThrows(PersistenceException.class, () -> ReflectionTools.hasPrimitiveTypeParameters(stringMap, 3));
        assertThrows(PersistenceException.class, () -> ReflectionTools.hasPrimitiveTypeParameters(stringMap, 0));
    }

    @Test
    void classAndTypeCanBeRetrieved() throws NoSuchMethodException {
        // GIVEN
        ParameterizedType type = (ParameterizedType) getGenericReturnType("getStringCollection");
        Class<?> someClass = String.class;

        assertNotNull(ReflectionTools.getClass(type));
        assertNotNull(ReflectionTools.getClass(someClass));
    }

    @Test
    void canDetectArrayDimensions() {
        Integer[] simpleArray = {1, 2, 3};
        Integer[][] array2d = {{1}, {2}, {3}};
        Integer[][][] array3d = {{{1}}};

        // THEN
        assertEquals(1, ReflectionTools.getArrayDimensions(simpleArray).length);
        assertEquals(3, ReflectionTools.getArrayDimensions(simpleArray)[0]);

        assertEquals(2, ReflectionTools.getArrayDimensions(array2d).length);
        assertEquals(3, ReflectionTools.getArrayDimensions(array2d)[0]);
        assertEquals(1, ReflectionTools.getArrayDimensions(array2d)[1]);

        assertEquals(3, ReflectionTools.getArrayDimensions(array3d).length);
        assertEquals(1, ReflectionTools.getArrayDimensions(array3d)[0]);
        assertEquals(1, ReflectionTools.getArrayDimensions(array3d)[1]);
        assertEquals(1, ReflectionTools.getArrayDimensions(array3d)[2]);

    }

    @Test
    void canCreateMultidimensionalArrayByTemplate() {
        // GIVEN
        Integer[] simpleArray = {1, 2, 3};
        Integer[][] array2d = {{1}, {2}, {3}};
        Integer[][][] array3d = {{{1}}};
        TestService[][] testService2d = {{new TestService()}};

        // WHEN
        Integer[] simpleResult = ReflectionTools.createEmptyArray(simpleArray, Integer[].class);
        Integer[][] result2d = ReflectionTools.createEmptyArray(array2d, Integer[][].class);
        Integer[][][] result3d = ReflectionTools.createEmptyArray(array3d, Integer[][][].class);
        TestService[][] resultTestService2d = ReflectionTools.createEmptyArray(testService2d, TestService[][].class);

        // THEN
        assertEquals(3, simpleResult.length);

        assertEquals(3, result2d.length);
        assertEquals(1, result2d[0].length);
        assertEquals(1, result2d[1].length);
        assertEquals(1, result2d[2].length);

        assertEquals(1, result3d.length);
        assertEquals(1, result3d[0].length);
        assertEquals(1, result3d[0][0].length);

        assertEquals(1, resultTestService2d.length);
        assertEquals(1, resultTestService2d[0].length);
    }


    @Test
    void componentTypeOfMultidimensionalArrayShouldBeDetected() {
        // GIVEN
        int[][][] ints3d = new int[0][0][0];
        int[] ints1d = new int[0];

        // THEN
        assertEquals(int.class, ReflectionTools.getRootComponentType(ints3d.getClass()));
        assertEquals(int.class, ReflectionTools.getRootComponentType(ints1d.getClass()));
    }


    @Test
    void primitivesCanBeParsedFromString() {
        // GIVEN
        String intString = Integer.toString(1);
        String longString = Long.toString(1L);
        String floatString = Float.toString(1.0f);
        String doubleString = Double.toString(1.0);
        String shortString = Short.toString((short) 1);
        String byteString = Byte.toString((byte) 1);
        String charString = Character.toString('1');

        // THEN
        assertEquals(1, (int) ReflectionTools.parseString(intString, Integer.class));
        assertEquals(1L, ReflectionTools.parseString(longString, Long.class));
        assertEquals(1.0f, ReflectionTools.parseString(floatString, Float.class));
        assertEquals(1.0, ReflectionTools.parseString(doubleString, Double.class));
        assertEquals((short) 1, ReflectionTools.parseString(shortString, Short.class));
        assertEquals((byte) 1, ReflectionTools.parseString(byteString, Byte.class));
        assertEquals('1', ReflectionTools.parseString(charString, Character.class));
        assertEquals("1", ReflectionTools.parseString("1", String.class));
    }

    /**
     * A Help-Method that is used to retrieve a {@link Class} from primitives using autoboxing.
     * @param obj The Object of which the Class is wanted
     * @return the Class of obj
     */
    private Class<?> toClass(Object obj) {
        return obj.getClass();
    }

    private Type getGenericReturnType(String methodName) throws NoSuchMethodException {
        Method method = TestService.class.getMethod(methodName);
        return method.getGenericReturnType();
    }

    private static class TestService {

        public Collection<String> getStringCollection() {
            return new ArrayList<>();
        }

        public Collection<Collection<String>> getCollectionOfCollections() {
            return new ArrayList<>();
        }

        public Map<String, String> getStringMap() {
            return new HashMap<>();
        }

        public Map<Date, Date> getDateMap() {
            return new HashMap<>();
        }
    }
}