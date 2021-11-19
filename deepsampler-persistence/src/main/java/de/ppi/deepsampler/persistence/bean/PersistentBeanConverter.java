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
 * <p>
 * The concrete serialization / Deserialization is done by an underlying persistence api. PersistentBeanConverter is only responsible to
 * to create an intermediate data structures for cases where the persistence api is not capable to serialize / deserialize the original
 * data on its own.
 */
public class PersistentBeanConverter {

    private final List<BeanConverterExtension> beanConverterExtensions = new ArrayList<>();

    public void addExtension(final BeanConverterExtension extension) {
        beanConverterExtensions.add(extension);
    }


    /**
     * Reverts an abstract model from the persistence to the original bean.
     *
     * @param persistentBean    an object that has been deserialized from a persistence api (e.g. some JSON-API). This object
     *                          might already be the original bean it the persistence api was able to deserialize it. Otherwise
     *                          it is the abstract model represented by {@link PersistentBean}
     * @param parameterizedType The Type of the original bean
     * @param <T>               the original bean.
     * @return the original deserialized bean.
     */
    @SuppressWarnings("unchecked")
    public <T> T revert(final Object persistentBean, final Class<T> originalBeanClass, final ParameterizedType parameterizedType) {
        if (persistentBean == null) {
            return null;
        }

        Class<?> persistentBeanClass = persistentBean.getClass();
        Class<?> persistentBeanComponentType = ReflectionTools.getRootComponentType(persistentBeanClass);

        if (persistentBeanClass.isArray() && PersistentBean.class.isAssignableFrom(persistentBeanComponentType)) {
            return (T) revertPersistentBeanArray(persistentBean, originalBeanClass);
        }

        final List<BeanConverterExtension> applicableExtensions = findApplicableExtensions(originalBeanClass, parameterizedType);
        if (!applicableExtensions.isEmpty()) {
            // Only use the first one!
            return applicableExtensions.get(0).revert(persistentBean, originalBeanClass, parameterizedType, this);
        }

        if (persistentBean instanceof PersistentBean) {
            return revertPersistentBean((PersistentBean) persistentBean, originalBeanClass);
        }

        return (T) persistentBean;
    }


    /**
     * Converts an original bean to the abstract model (most likely {@link PersistentBean} that is used to save the original bean to e.g. JSON.
     *
     * @param originalBean The original Bean that is supposed to be persisted.
     * @param <T>          The type of the persistent bean.
     * @return The object that will be sent to the underlying persistence api. This might be a {@link PersistentBean} or the original bean if
     * the persistence api is expected to be able to serialize the original bean directly.
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(final Object originalBean, Type type) {
        final ParameterizedType parameterizedReturnType = type instanceof ParameterizedType ? (ParameterizedType) type : null;
        if (isTransformationNotNecessary(originalBean, parameterizedReturnType)) {
            return (T) originalBean;
        }
        /**
         * Todo
         * 1. Transferobjekt für Extensionschnittstelle mit den Typen bauen
         * 2. Issue für die Erweiterung der Extension schreiben und zur Diskussion stellen
         *
         */
        if (originalBean.getClass().isArray()) {
            return (T) convertObjectArray((Object[]) originalBean);
        }


        final List<BeanConverterExtension> applicableExtensions = findApplicableExtensions(originalBean.getClass(), parameterizedReturnType);
        if (!applicableExtensions.isEmpty()) {
            // Only use the first one!
            return (T) applicableExtensions.get(0).convert(originalBean, parameterizedReturnType, this);
        }

        if (!originalBean.getClass().equals(type)) {
            return (T) convertToPolymorphicPersistentBean(originalBean.getClass().getTypeName(), originalBean);
        }

        return (T) convertToPersistentBean(originalBean);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] revertPersistentBeanArray(final Object persistentBeanArray, final Class<T> componentType) {
        Object originalBeansArray = ReflectionTools.createEmptyArray(persistentBeanArray, componentType);

        for (int i = 0; i < Array.getLength(persistentBeanArray); ++i) {
            Object persistentEntry = Array.get(persistentBeanArray, i);

            Object entry;
            if (persistentEntry.getClass().isArray()) {
                entry = revertPersistentBeanArray(persistentEntry, componentType.getComponentType());
            } else {
                entry = revertPersistentBean((PersistentBean) persistentEntry, componentType.getComponentType());
            }
            Array.set(originalBeansArray, i, entry);
        }

        return (T[]) originalBeansArray;
    }

    @SuppressWarnings("unchecked")
    private <T> T revertPersistentBean(final PersistentBean value, final Class<T> declaredOriginalBeanClass) {
        final Class<T> valueType;
        if (value instanceof PolymorphicPersistentBean) {
            valueType = (Class<T>) ReflectionTools.getOriginalClassFromPolymorphicPersistentBean((PolymorphicPersistentBean) value);
        } else {
            valueType = declaredOriginalBeanClass;
        }

        final T instance;
        final Map<Field, String> fields = getAllFields(valueType);

        if (hasFinalFields(fields)) {
            instance = instantiateUsingMatchingConstructor(valueType, value, fields);
        } else {
            instance = instantiate(valueType);

            for (final Map.Entry<Field, String> entry : fields.entrySet()) {
                final Field field = entry.getKey();
                final String key = entry.getValue();

                transferFieldFromBean(value, instance, field, key);
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

    private <T> void transferFieldFromBean(final PersistentBean persistentBean, final T instance, final Field field, final String fieldKeyInPersistentBean) {
        Object lookedUpValueInBean = persistentBean.getValue(fieldKeyInPersistentBean);
        if (lookedUpValueInBean != null) {
            if (lookedUpValueInBean instanceof PersistentBean) {
                lookedUpValueInBean = revertPersistentBean((DefaultPersistentBean) lookedUpValueInBean, field.getType());
            } else if (lookedUpValueInBean.getClass().isArray() && PersistentBean.class.isAssignableFrom(lookedUpValueInBean.getClass().getComponentType())) {
                lookedUpValueInBean = revertPersistentBeanArray(lookedUpValueInBean, field.getType());
            }
            setValue(instance, field, lookedUpValueInBean);
        }
    }

    private <T> T instantiate(final Class<T> cls) {
        final Objenesis objenesis = new ObjenesisStd();
        final ObjectInstantiator<T> instantiatorOf = objenesis.getInstantiatorOf(cls);
        return instantiatorOf.newInstance();
    }


    private Object convertObjectArray(final Object[] objects) {
        int[] dimensions = ReflectionTools.getArrayDimensions(objects);
        Class<?> componentType = Array.newInstance(PersistentBean.class, dimensions).getClass();
        Object persistentBeans = ReflectionTools.createEmptyArray(objects, componentType);

        for (int i = 0; i < objects.length; ++i) {
            Object subElement = convert(objects[i], null);
            Array.set(persistentBeans, i, subElement);
        }

        return persistentBeans;
    }

    private PersistentBean convertToPolymorphicPersistentBean(final String type, final Object obj) {
        final Map<String, Object> valuesForBean = getValueMapForObjects(obj);

        return new PolymorphicPersistentBean(valuesForBean, type);
    }


    private PersistentBean convertToPersistentBean(final Object obj) {
        final Map<String, Object> valuesForBean = getValueMapForObjects(obj);

        return new DefaultPersistentBean(valuesForBean);
    }

    private Map<String, Object> getValueMapForObjects(Object obj) {
        final Map<Field, String> fieldStringMap = getAllFields(obj.getClass());

        final Map<String, Object> valuesForBean = new HashMap<>();
        for (final Map.Entry<Field, String> entry : fieldStringMap.entrySet()) {
            final String keyForField = entry.getValue();
            final Field field = entry.getKey();
            Object fieldValue = retrieveValue(obj, field);

            final Type fieldType = field.getGenericType();
            final ParameterizedType parameterizedFieldType = fieldType instanceof ParameterizedType ? (ParameterizedType) fieldType : null;

            if (fieldValue != null) {
                fieldValue = convert(fieldValue, parameterizedFieldType);
            }

            valuesForBean.put(keyForField, fieldValue);
        }
        return valuesForBean;
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


    private List<BeanConverterExtension> findApplicableExtensions(final Class<?> beanClass, final ParameterizedType parameterizedType) {
        return beanConverterExtensions.stream().filter(ext -> ext.isProcessable(beanClass, parameterizedType)).collect(Collectors.toList());
    }

    private boolean isTransformationNotNecessary(final Object obj, final ParameterizedType parameterizedType) {

        return obj == null || ReflectionTools.isPrimitiveOrWrapper(obj.getClass()) || (!ReflectionTools.isObjectArray(obj.getClass()) && obj.getClass().isArray())
                || findApplicableExtensions(obj.getClass(), parameterizedType).stream()
                .anyMatch(ext -> ext.skip(obj.getClass(), parameterizedType));
    }


}