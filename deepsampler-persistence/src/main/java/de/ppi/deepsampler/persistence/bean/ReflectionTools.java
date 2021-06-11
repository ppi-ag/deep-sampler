package de.ppi.deepsampler.persistence.bean;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.Collection;

/**
 * A collection of utility methods for frequently occurring reflection problems.
 */
public class ReflectionTools {

    /**
     * Checks if cls is an Array with a complex component type. This is the case if the component type is not a primitive type
     * nor one of its wrapper types.
     *
     * @param cls The class of the suspected array
     * @return true if cls is an array with elements that have a Type that is not primitive nor a primitve wrapper.
     */
    public static boolean isObjectArray(final Class<?> cls) {
        return cls.isArray() && !(cls == int[].class
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

        return  parameterizedType.getRawType() instanceof Class<?>
                && Collection.class.isAssignableFrom((Class<?>)parameterizedType.getRawType())
                && isPrimitiveOrWrapper((Class<?>) (parameterizedType.getActualTypeArguments()[0]));
    }

    /**
     * Returns the {@link Class} behind type.
     * @param type The type for which the Class is wanted.
     * @param <T> The type of the Class
     * @return If type is a {@link ParameterizedType}, the raw Class is returned, otherwise a casted version of type.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) type).getRawType();
        }

        return (Class<T>) type;
    }

}
