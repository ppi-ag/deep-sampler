/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean;

import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.model.PersistentBean;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DeepSampler saves Beans by converting them in an abstract model that enables DeepSampler to omit type information in persistent files.
 * This approach makes persistent beans less vulnerable to refactorings. E.g. it is not necessary to rename classes in persistent Sample-files
 * if classes are renamed during refactorings.
 *
 * The concrete serialization / Deserialization is done by an underlying persistence api. PersistentBeanConverter is only responsible to
 * to create an intermediate data structures for cases where the persistence api is not capable to serialize / deserialize the original
 * data on its own.
 *
 */
public class PersistentBeanConverter {

    private final List<BeanConverterExtension> beanConverterExtensions = new ArrayList<>();

    public void addExtension(final BeanConverterExtension extension) {
        beanConverterExtensions.add(extension);
    }


    /**
     * Reverts an abstract model from the persistence to the original bean.
     *
     * @param persistentBean an object that has been deserialized from a persistence api (e.g. some JSON-API). This object
     *                       might already be the original bean it the persistence api was able to deserialize it. Otherwise
     *                       it is the abstract model represented by {@link PersistentBean}
     * @param type The Type of the original bean
     * @param <T> the original bean.
     * @return the original deserialized bean.
     */
    @SuppressWarnings("unchecked")
    public <T> T revert(final Object persistentBean, final Type type) {
        if (persistentBean == null) {
            return null;
        }

        if (persistentBean.getClass().isArray() && PersistentBean.class.isAssignableFrom(persistentBean.getClass().getComponentType())) {
            final Class<T> componentType =  (Class<T>) ReflectionTools.getClass(type).getComponentType();
            return (T) revertPersistentBeanArray((PersistentBean[]) persistentBean, componentType);
        }

        final List<BeanConverterExtension> applicableExtensions = findApplicableExtensions(type);
        if (!applicableExtensions.isEmpty()) {
            // Only use the first one!
            return applicableExtensions.get(0).revert(persistentBean, type, this);
        }

        if (persistentBean instanceof PersistentBean) {
            return revertPersistentBean((PersistentBean) persistentBean, type);
        }

        return (T) persistentBean;
    }

    /**
     * Converts an original bean to the abstract model (most likely {@link PersistentBean} that is used to save the original bean to e.g. JSON.
     * @param originalBean The original Bean that is supposed to be persisted.
     * @param <T> The type of the persistent bean.
     * @return The object that will be sent to the underlying persistence api. This might be a {@link PersistentBean} or the original bean if
     * the persistence api is expected to be able to serialize the original bean directly.
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(final Object originalBean) {
        if (isTransformationNotNecessary(originalBean)) {
            return (T) originalBean;
        }

        if (originalBean.getClass().isArray()) {
            return (T) convertArray((Object[]) originalBean);
        }

        final List<BeanConverterExtension> applicableExtensions = findApplicableExtensions(originalBean.getClass());
        if (!applicableExtensions.isEmpty()) {
            // Only use the first one!
            return (T) applicableExtensions.get(0).convert(originalBean, this);
        }

        return (T) convertToPersistentBean(originalBean);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] revertPersistentBeanArray(final PersistentBean[] persistentBean, final Class<T> cls) {
        final T[] instances = (T[]) Array.newInstance(cls, persistentBean.length);
        for (int i = 0; i < persistentBean.length; ++i) {
            instances[i] = revertPersistentBean(persistentBean[i], cls);
        }
        return instances;
    }

    private <T> T revertPersistentBean(final PersistentBean value, final Type type) {
        final T instance;
        final Class<T> rawClass = ReflectionTools.getClass(type);
        final Map<Field, String> fields = getAllFields(rawClass);

        if (hasFinalFields(fields)) {
            instance = instantiateUsingMatchingConstructor(rawClass, value, fields);
        } else {
            instance = instantiate(rawClass);

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
                lookedUpValueInBean = revertPersistentBean((DefaultPersistentBean) lookedUpValueInBean, field.getType());
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
                lookedUpValueInBean = revertPersistentBean((DefaultPersistentBean) lookedUpValueInBean, field.getType());
            } else if (lookedUpValueInBean.getClass().isArray() && PersistentBean.class.isAssignableFrom(lookedUpValueInBean.getClass().getComponentType())) {
                lookedUpValueInBean = revertPersistentBeanArray((PersistentBean[]) lookedUpValueInBean, field.getType().getComponentType());
            }
            setValue(instance, field, lookedUpValueInBean);
        }
    }

    private <T> T instantiate(final Class<T> cls) {
        final Objenesis objenesis = new ObjenesisStd();
        final ObjectInstantiator<T> instantiatorOf = objenesis.getInstantiatorOf(cls);
        return instantiatorOf.newInstance();
    }



    private Object[] convertArray(final Object[] objects) {
        final PersistentBean[] persistentBeans = new PersistentBean[objects.length];
        for (int i = 0; i < objects.length; ++i) {
            persistentBeans[i] = convert(objects[i]);
        }
        return persistentBeans;
    }


    private PersistentBean convertToPersistentBean(final Object obj) {
        final Map<Field, String> fieldStringMap = getAllFields(obj.getClass());

        final Map<String, Object> valuesForBean = new HashMap<>();
        for (final Map.Entry<Field, String> entry : fieldStringMap.entrySet()) {
            final String keyForField = entry.getValue();
            final Field field = entry.getKey();
            Object fieldValue = retrieveValue(obj, field);

            if (fieldValue != null) {
                fieldValue = convert(fieldValue);
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



    private List<BeanConverterExtension> findApplicableExtensions(final Type type) {
        return beanConverterExtensions.stream().filter(ext -> ext.isProcessable(type)).collect(Collectors.toList());
    }

    private boolean isTransformationNotNecessary(final Object obj) {

        return obj == null || ReflectionTools.isPrimitiveOrWrapper(obj.getClass()) || (!ReflectionTools.isObjectArray(obj.getClass()) && obj.getClass().isArray())
                || findApplicableExtensions(obj.getClass()).stream().anyMatch(ext -> ext.skip(obj.getClass()));
    }





}