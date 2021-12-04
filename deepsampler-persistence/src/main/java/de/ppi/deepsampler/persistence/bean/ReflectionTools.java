/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean;

import de.ppi.deepsampler.persistence.error.PersistenceException;

import java.lang.reflect.*;

import java.util.ArrayList;
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
     * nor one of its wrapper types. Multidimensional arrays, thus arrays in arrays are also treated as primitive arrays if the component type
     * of the deepest not array type is a primitive.
     *
     * @param cls The class of the suspected array
     * @return true if cls is an array with elements that have a Type that is not primitive nor a primitive wrapper.
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
     * @param cls The suspected primitive type
     * @return true it cls is a primitive or a wrapper like int and Integer.
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
     * Checks if type has a generic type parameter which is a primitive or its wrapper like {@link Integer}.
     * (Real primitives cannot occur here, because primitives are not allowed as generic type parameters.)
     *
     * @param type The generic class that should be checked.
     * @return true if cls has a generic type parameter that is a wrapper type.
     */
    public static boolean hasPrimitiveTypeParameters(Type type) {
        return hasPrimitiveTypeParameters(type, 1);
    }

    /**
     * Checks if type has generic type parameters which are a primitive or its wrapper like {@link Integer}.
     * (Real primitives cannot occur here, because primitives are not allowed as generic type parameters.)
     *
     * @param type                      The generic class that should be checked.
     * @param numberOfParametersToCheck The number of the first generic parameters that should be checked.
     * @return true if cls has generic type parameters that are wrapper types.
     */
    public static boolean hasPrimitiveTypeParameters(Type type, int numberOfParametersToCheck) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;

        if (numberOfParametersToCheck > parameterizedType.getActualTypeArguments().length) {
            throw new PersistenceException("We tried to check if the first %d generic type parameter of %s were primitive wrappers, but we have only %d parameters",
                    numberOfParametersToCheck, type.getTypeName(), parameterizedType.getActualTypeArguments().length);
        }

        if (numberOfParametersToCheck == 0) {
            throw new PersistenceException("numberOfParameterToCheck must be > 0. It is not the index, but the number of the first parameters that should be checked.");
        }

        if (parameterizedType.getRawType() instanceof Class<?>) {
            for (int i = 0; i < numberOfParametersToCheck; i++) {
                Type parameter = parameterizedType.getActualTypeArguments()[i];

                if (parameter instanceof Class) {
                    if (!isPrimitiveOrWrapper((Class<?>) parameter)) {
                        return false;
                    }
                } else if (parameter instanceof ParameterizedType) {
                    return false;
                } else {
                    throw new PersistenceException("We cannot determine the generic type parameter of %s because the actualTypeArgument is %s. Instead, we need a %s." +
                            " This can be achieved e.g. by retrieving the type from Class::getMethod()::getGenericReturnType()",
                            type.getTypeName(), parameterizedType.getActualTypeArguments()[0].getTypeName(), Class.class.getTypeName());
                }
            }
        }

        return true;
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
     * @param templateArray A template array that is used as an example for the dimensions of the new array.
     * @param newArrayType  the componentType of the new array
     * @param <T>           the componentType of the new array
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
     *
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

    /**
     * Converts the String source into a primitive wrapper object using the supplied wrapperType.
     *
     * @param source      The String that should be converted to a wrapperType. The String must be formatted in a way that complies with
     *                    the parser of the desired wrapper type.
     * @param wrapperType A wrapper type. This must be a Class of any Wrapper type otherwise an Exception will be thrown.
     * @param <T>         The target type.
     * @return returns an instance of wrapperType containing the parsed value of source.
     */
    public static <T> T parseString(String source, Class<T> wrapperType) {
        Constructor<T> constructor;

        try {
            if (Character.class == wrapperType) {
                constructor = wrapperType.getConstructor(char.class);
                return constructor.newInstance(source.charAt(0));
            } else {
                constructor = wrapperType.getConstructor(String.class);
                return constructor.newInstance(source);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PersistenceException("We were unable to parse %s from %s", e, wrapperType.getTypeName(), source);
        }
    }

    /**
     * Retrieves the original type from polymorphicPersistentBean and tries to instantiate a {@link Class} for the type.
     * If the {@link Class} cannot be found a {@link PersistenceException} is thrown.
     *
     * @param polymorphicPersistentBean The {@link PolymorphicPersistentBean} which holds the wanted type.
     * @return the class of the original bean that is described by polymorphicPersistentBean.
     */
    public static Class<?> getOriginalClassFromPolymorphicPersistentBean(PolymorphicPersistentBean polymorphicPersistentBean) {
        try {
            return Class.forName(polymorphicPersistentBean.getPolymorphicBeanType());
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(
                    "The Polymorphic Class %s was not found. This occurs if a polymorphic class was recorded but is not in the classpath (anymore?)",
                    e,
                    polymorphicPersistentBean.getPolymorphicBeanType());
        }
    }

}
