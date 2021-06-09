/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean;

import de.ppi.deepsampler.persistence.bean.ext.*;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.model.PersistentBean;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class PersistentBeanFactory {

    private final List<BeanFactoryExtension> beanFactoryExtensions = new ArrayList<>();

    public PersistentBeanFactory() {
        addExtension(new JavaTimeExtension());
        addExtension(new CollectionExtension());
        addExtension(new ArrayExtension());
        addExtension(new MapWithStringKeyExtension());
        //addExtension(new CollectionMapExtension());
    }

    public void addExtension(final BeanFactoryExtension extension) {
        beanFactoryExtensions.add(extension);
    }

    @SuppressWarnings("unchecked")
    public <T> T[] ofBean(final PersistentBean[] persistentBean, final Class<T> cls) {
        final T[] instances = (T[]) Array.newInstance(cls, persistentBean.length);
        for (int i = 0; i < persistentBean.length; ++i) {
            instances[i] = createValueFromPersistentBean(persistentBean[i], cls);
        }
        return instances;
    }

    @SuppressWarnings("unchecked")
    public <T> T convertValueFromPersistentBeanIfNecessary(final Object value, final Type type) {
        if (value instanceof PersistentBean || value instanceof Collection || value instanceof Map || (value != null && value.getClass().isArray())) {
            return createValueFromPersistentBean(value, type);
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    public <T> T createValueFromPersistentBean(final Object value, final Type type) {
        final List<BeanFactoryExtension> applicableExtensions = findApplicableExtensions(type);
        if (!applicableExtensions.isEmpty()) {
            // Only use the first one!
            BeanFactoryExtension extension = applicableExtensions.get(0);
            return extension.ofBean(value, type);
        }

        final T instance;
        Class<T> targetClass = type instanceof Class ? (Class<T>) type : (Class<T>) ((ParameterizedType) type).getRawType();
        final Map<Field, String> fields = getAllFields(targetClass);

        if (hasFinalFields(fields)) {
            instance = instantiateUsingMatchingConstructor(targetClass, (PersistentBean) value, fields);
        } else {
            instance = instantiate(targetClass);

            for (final Map.Entry<Field, String> entry : fields.entrySet()) {
                final Field field = entry.getKey();
                final String key = entry.getValue();

                transferFromBean((PersistentBean) value, instance, field, key);
            }
        }
        return instance;
    }

    private <T> T instantiateUsingMatchingConstructor(final Class<T> type,
                                                      final PersistentBean persistentBean,
                                                      final Map<Field, String> fields) {
        try {
            return createInstance(type, persistentBean, fields);

        } catch (final NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new PersistenceException("The type %s includes at least one final field. Therefore we tried to automatically detect a " +
                    "constructor accepting all field values, but weren't able to find any. If you still want to transform the bean you have to implement a BeanFactoryExtension" +
                    " which is able to construct the desired type %s.", e, type, type);
        }
    }

    private <T> T createInstance(final Class<T> type, final PersistentBean persistentBean, final Map<Field, String> fields) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Class<?>[] parameterTypes = fields.keySet()
                .stream()
                .map(Field::getType)
                .toArray(Class[]::new);

        final List<Object> values = createValuesForConstructingInstance(persistentBean, fields);
        return type.getDeclaredConstructor(parameterTypes)
                .newInstance(values.toArray());
    }

    private List<Object> createValuesForConstructingInstance(final PersistentBean persistentBean, final Map<Field, String> fields) {
        final List<Object> values = new ArrayList<>();

        for (final Map.Entry<Field, String> entry : fields.entrySet()) {
            final Field field = entry.getKey();
            final String key = entry.getValue();

            Object lookedUpValueInBean = persistentBean.getValue(key);
            if (lookedUpValueInBean instanceof DefaultPersistentBean) {
                lookedUpValueInBean = createValueFromPersistentBean(lookedUpValueInBean, field.getType());
            }
            values.add(lookedUpValueInBean);
        }
        return values;
    }

    private boolean hasFinalFields(final Map<Field, String> fields) {
        return fields.entrySet()
                .stream()
                .anyMatch(entry -> Modifier.isFinal(entry.getKey().getModifiers()));
    }

    private <T> void transferFromBean(final PersistentBean persistentBean, final T instance, final Field field, final String key) {
        Object lookedUpValueInBean = persistentBean.getValue(key);
        if (lookedUpValueInBean != null) {
            if (lookedUpValueInBean instanceof DefaultPersistentBean) {
                lookedUpValueInBean = createValueFromPersistentBean(lookedUpValueInBean, field.getType());
            }
            setValue(instance, field, lookedUpValueInBean);
        }
    }

    private <T> T instantiate(final Class<T> cls) {
        final Objenesis objenesis = new ObjenesisStd();
        final ObjectInstantiator<T> instantiatorOf = objenesis.getInstantiatorOf(cls);
        return instantiatorOf.newInstance();
    }

    @SuppressWarnings("unchecked")
    public <T> T toBean(final Object obj, Type beanType) {
        final List<BeanFactoryExtension> applicableExtensions = findApplicableExtensions(beanType);
        if (!applicableExtensions.isEmpty()) {
            // Only use the first one!
            return applicableExtensions.get(0).toBean(obj, beanType);
        }

        final Map<Field, String> fieldStringMap = getAllFields(obj.getClass());

        final Map<String, Object> valuesForBean = new HashMap<>();
        for (final Map.Entry<Field, String> entry : fieldStringMap.entrySet()) {
            final String keyForField = entry.getValue();
            final Field field = entry.getKey();
            Object fieldValue = retrieveValue(obj, field);
            Type fieldType = field.getGenericType();

            if (fieldValue != null) {
                fieldValue = toBeanIfNecessary(fieldValue, fieldType);
            }

            valuesForBean.put(keyForField, fieldValue);
        }

        return (T) new DefaultPersistentBean(valuesForBean);
    }

    @SuppressWarnings("java:S3011") // We need the possibility to set the values of private fields for deserialization.
    private void setValue(final Object obj, final Field field, final Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("java:S3011") // We need the possibility to get the values of private fields for serialization.
    private Object retrieveValue(final Object obj, final Field field) {
        final Object fieldValue;
        try {
            field.setAccessible(true);
            fieldValue = field.get(obj);
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        return fieldValue;
    }

    public static boolean isPrimitiveOrWrapperArray(final Class<?> cls) {
        return cls.isArray() && (cls == int[].class
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

    public static boolean isPrimitiveOrWrapperList(Type type) {
        if (type instanceof ParameterizedType) {
            // Lists must always be a generic type...
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();

            if (typeArguments.length == 1) {
                // a List<> has only one parameter...
                Type typeArgument = typeArguments[0];
                if (typeArgument instanceof Class<?>) {
                    // if typeArgument is not a Class<> it is not a primitve or wrapper...
                    return isPrimitiveOrWrapper((Class<?>) typeArgument);
                }
            }
        }

        return false;
    }

    public static boolean isPrimitiveOrWrapperMap(Type type) {
        if (type instanceof ParameterizedType) {
            // Lists must always be a generic type...
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();

            if (typeArguments.length == 2) {
                // a Map<> must have two parameters...
                Type keyTyp = typeArguments[0];
                Type valueTyp = typeArguments[1];
                if (keyTyp instanceof Class<?> && valueTyp instanceof Class<?>) {
                    // if a type is not a Class<> it is not a primitve or wrapper...
                    return isPrimitiveOrWrapper((Class<?>) keyTyp) && isPrimitiveOrWrapper((Class<?>) valueTyp);
                }
            }
        }

        return false;
    }

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

    private Map<Field, String> getAllFields(final Class<?> cls) {
        final Map<Field, String> fields = new LinkedHashMap<>();
        Class<?> currentCls = cls;
        int depth = 0;
        while (currentCls != null) {
            for (final Field field : currentCls.getDeclaredFields()) {
                if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
                    fields.put(field, String.format("%s$%s", depth, field.getName()));
                }
            }
            depth += 1;
            currentCls = currentCls.getSuperclass();
        }
        return fields;
    }

    public Object toBeanIfNecessary(final Object obj, Type objType) {
        if (isTransformationNotNecessary(obj, objType)) {
            return obj;
        }

        return toBean(obj, objType);
    }

    private List<BeanFactoryExtension> findApplicableExtensions(final Type beanType) {
        return beanFactoryExtensions.stream()
                .filter(ext -> ext.isProcessable(beanType))
                .collect(Collectors.toList());
    }

    private boolean isTransformationNotNecessary(final Object obj, Type objType) {

        return obj == null || isPrimitiveOrWrapper(obj.getClass())
                || isSkippedByExtension(objType);
    }

    private boolean isSkippedByExtension(Type objType) {
        return findApplicableExtensions(objType).stream().anyMatch(ext -> ext.skip(objType));
    }

    public List<Object> toBeanIfNecessary(final List<Object> objectList) {
        return objectList.stream()
                .map(obj -> toBeanIfNecessary(obj, obj.getClass()))
                .collect(Collectors.toList());
    }


}