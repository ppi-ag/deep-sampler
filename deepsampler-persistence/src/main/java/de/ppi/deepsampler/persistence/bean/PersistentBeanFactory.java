/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean;

import de.ppi.deepsampler.persistence.bean.ext.BeanFactoryExtension;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.model.PersistentBean;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class PersistentBeanFactory {

    private final List<BeanFactoryExtension> beanFactoryExtensions = new ArrayList<>();

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
    public <T> T convertValueFromPersistentBeanIfNecessary(final Object value, final Class<T> type) {
        if (value instanceof PersistentBean) {
            return createValueFromPersistentBean((PersistentBean) value, type);
        }
        return (T) value;
    }

    public <T> T createValueFromPersistentBean(final PersistentBean value, final Class<T> type) {
        final List<BeanFactoryExtension> applicableExtensions = findApplicableExtensions(type);
        if (!applicableExtensions.isEmpty()) {
            // Only use the first one!
            return applicableExtensions.get(0).ofBean(value, type);
        }

        final T instance;
        final Map<Field, String> fields = getAllFields(type);

        if (hasFinalFields(fields)) {
            instance = instantiateUsingMatchingConstructor(type, value, fields);
        } else {
            instance = instantiate(type);

            for (final Map.Entry<Field, String> entry : fields.entrySet()) {
                final Field field = entry.getKey();
                final String key = entry.getValue();

                transferFromBean(value, instance, field, key);
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
            throw new PersistenceException("While the type %s has final fields, it was " +
                    "tried to use a matching constructor for all field-values. Because this" +
                    "was not possbile, please provide a BeanFactoryExtension.", e, type);
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
                lookedUpValueInBean = createValueFromPersistentBean((DefaultPersistentBean) lookedUpValueInBean, field.getType());
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
                lookedUpValueInBean = createValueFromPersistentBean((DefaultPersistentBean) lookedUpValueInBean, field.getType());
            }
            setValue(instance, field, lookedUpValueInBean);
        }
    }

    private <T> T instantiate(final Class<T> cls) {
        final Objenesis objenesis = new ObjenesisStd();
        final ObjectInstantiator<T> instantiatorOf = objenesis.getInstantiatorOf(cls);
        return instantiatorOf.newInstance();
    }

    public PersistentBean toBean(final Object obj) {
        final List<BeanFactoryExtension> applicableExtensions = findApplicableExtensions(obj.getClass());
        if (!applicableExtensions.isEmpty()) {
            // Only use the first one!
            return applicableExtensions.get(0).toBean(obj);
        }

        final Map<Field, String> fieldStringMap = getAllFields(obj.getClass());

        final Map<String, Object> valuesForBean = new HashMap<>();
        for (final Map.Entry<Field, String> entry : fieldStringMap.entrySet()) {
            final String keyForField = entry.getValue();
            final Field field = entry.getKey();
            Object fieldValue = retrieveValue(obj, field);

            if (fieldValue != null) {
                if (isObjectArray(field.getType())) {
                    fieldValue = toBeanIfNecessary((Object[]) fieldValue);
                } else if (!isPrimitive(field.getType()) && !field.getType().isArray()) {
                    fieldValue = toBeanIfNecessary(fieldValue);
                }
            }
            valuesForBean.put(keyForField, fieldValue);
        }

        return new DefaultPersistentBean(valuesForBean);
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

    private boolean isObjectArray(final Class<?> cls) {
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

    private boolean isPrimitive(final Class<?> cls) {
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

    public Object toBeanIfNecessary(final Object obj) {
        return isTransformationNotNecessary(obj) ? obj : toBean(obj);
    }

    private List<BeanFactoryExtension> findApplicableExtensions(final Class<?> cls) {
        return beanFactoryExtensions.stream().filter(ext -> ext.isProcessable(cls)).collect(Collectors.toList());
    }

    private boolean isTransformationNotNecessary(final Object obj) {

        return obj == null || isPrimitive(obj.getClass()) || (!isObjectArray(obj.getClass()) && obj.getClass().isArray())
                || findApplicableExtensions(obj.getClass()).stream().anyMatch(ext -> ext.skip(obj.getClass()));
    }

    public List<Object> toBeanIfNecessary(final List<Object> objectList) {
        return objectList.stream()
                .map(this::toBeanIfNecessary)
                .collect(Collectors.toList());
    }

    public Object[] toBeanIfNecessary(final Object[] objects) {
        final Object[] persistentBeans = new Object[objects.length];
        for (int i = 0; i < objects.length; ++i) {
            persistentBeans[i] = toBeanIfNecessary(objects[i]);
        }
        return persistentBeans;
    }

}