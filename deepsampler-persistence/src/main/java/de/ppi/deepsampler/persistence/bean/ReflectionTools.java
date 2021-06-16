package de.ppi.deepsampler.persistence.bean;

import de.ppi.deepsampler.persistence.error.PersistenceException;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A collection of utility methods for frequently occurring reflection problems.
 */
public class ReflectionTools {

    private ReflectionTools() {
        // Private constructor to emphasize the utility nature of this static class.
    }

    /**
     * Checks if cls is an Array with a complex component type. This is the case if the component type is not a primitive type
     * nor one of its wrapper types. Multidimensional arrays, thus arrays in arrays are also treated as primitve arrays if the component type
     * of the deepest not array type is a primitve.
     *
     * @param cls The class of the suspected array
     * @return true if cls is an array with elements that have a Type that is not primitive nor a primitve wrapper.
     */
    public static boolean isObjectArray(final Class<?> cls) {
        if (!cls.isArray()) {
            return false;
        }

        if (cls.getComponentType() != null && cls.getComponentType().isArray()) {
            return isObjectArray(cls.getComponentType());
        }

        return !(cls == int[].class
                || cls == Integer[].class
                || cls == boolean[].class
                || cls == Boolean[].class
                || cls == byte[].class
                || cls == Byte[].class
                || cls == short[].class
                || cls == Short[].class
                || cls == long[].class
                || cls == Long[].class
                || cls == char[].class
                || cls == String[].class
                || cls == Character[].class
                || cls == Float[].class
                || cls == float[].class
                || cls == Double[].class
                || cls == double[].class);
    }

    /**
     * Checks if cls is a primitive type or one of its wrapper types i.e. Integer for int.
     *
     * @param cls The suspected primitve type
     * @return true it cls is a primitve or a wrapper like int and Integer.
     */
    public static boolean isPrimitiveOrWrapper(final Class<?> cls) {
        return cls.isPrimitive()
                || cls == Integer.class
                || cls == Boolean.class
                || cls == Byte.class
                || cls == Short.class
                || cls == Long.class
                || cls == String.class
                || cls == Character.class
                || cls == Float.class
                || cls == Double.class;
    }

    /**
     * Checks if type is a {@link Collection} with a primitive type wrapper like {@link Integer} as generic type parameter.
     * (Real primitives cannot occur here, because primitives are not allowed as generic type parameters.)
     *
     * @param type The class of the suspected Collection
     * @return true if cls is an Collection with elements that have a wrapper type.
     */
    public static boolean isPrimitiveWrapperCollection(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;

        if (parameterizedType.getRawType() instanceof Class<?>) {

            if (parameterizedType.getActualTypeArguments()[0] instanceof Class) {
                return isPrimitiveOrWrapper((Class<?>) (parameterizedType.getActualTypeArguments()[0]));
            } else if (parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType) {
                return false;
            } else {
                throw new PersistenceException("We cannot determine the generic type parameter of %s because the actualTypeArgument is %s. Instead, we need a %s." +
                        " This can be achieved e.g. by retrieving the type from Class::getMethod()::getGenericReturnType()",
                        type.getTypeName(), parameterizedType.getActualTypeArguments()[0].getTypeName(), Class.class.getTypeName());
            }
        }

        return false;
    }

    /**
     * Returns the {@link Class} behind type.
     *
     * @param type The type for which the Class is wanted.
     * @param <T>  The type of the Class
     * @return If type is a {@link ParameterizedType}, the raw Class is returned, otherwise a casted version of type.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) type).getRawType();
        }

        return (Class<T>) type;
    }


    /**
     * Creates an empty array. The dimensions of the new array will be the same as of templateArray. The componentType of the
     * new array will be newArrayType.
     *
     * @param templateArray A template array that is used as an example for the deimensions of the new array.
     * @param newArrayType the componentType of the new array
     * @param <T> the componentType of the new array
     * @return the new array formed after templateArray and newArrayType.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createEmptyArray(Object templateArray, Class<T> newArrayType) {
        Object copy = Array.newInstance(newArrayType.getComponentType(), Array.getLength(templateArray));

        if (newArrayType.getComponentType() != null && newArrayType.getComponentType().isArray()) {
            for (int i = 0; i < Array.getLength(templateArray); i++) {
                Object subArray = createEmptyArray(Array.get(templateArray, i), newArrayType.getComponentType());
                Array.set(copy, i, subArray);
            }
        }

        return (T) copy;
    }

    /**
     * Finds the dimensions of the array array.
     * @param array the array of which the dimensions should be retrieved
     * @return the dimensions of the array. Each entry in the returned array is the size of one dimension. The length of the array is the number of dimensions.
     */
    public static int[] getArrayDimensions(Object array) {
        List<Integer> dimensions = new ArrayList<>();

        dimensions.add(Array.getLength(array));

        Class<?> componentType = array.getClass().getComponentType();
        Object subArray = array;

        while (componentType.isArray()) {
            subArray = Array.get(subArray, 0);
            dimensions.add(Array.getLength(subArray));
            componentType = subArray.getClass().getComponentType();
        }

        int[] dimensionsArray = new int[dimensions.size()];
        for (int i = 0; i < dimensionsArray.length; i++) {
            dimensionsArray[i] = dimensions.get(i);
        }
        return dimensionsArray;
    }

    /**
     * Finds the root component type of the array array. The root component type is the deepest component type that is not an array.
     * This method is useful to retrieve the component type of a multidimensional array, but it also works with 1d arrays.
     *
     * @param array the array of which we want to find the component type
     * @return the component type.
     */
    public static Class<?> getRootComponentType(Class<?> array) {
        Class<?> componentType = array.getComponentType();

        if (componentType == null) {
            return array;
        }

        if (getClass(componentType).isArray()) {
            return getRootComponentType(getClass(componentType).getComponentType());
        }

        return componentType;
    }


}
