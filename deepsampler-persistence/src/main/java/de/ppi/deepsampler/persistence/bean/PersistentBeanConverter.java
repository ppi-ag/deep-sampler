/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean;

import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;
import de.ppi.deepsampler.persistence.bean.ext.StandardBeanConverterExtension;
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
     * <p>
     * If more than one applicable {@link BeanConverterExtension} is found, the last registered one will be used.
     *
     * @param persistentBean    an object that has been deserialized by a persistence api (e.g. some JSON-API). This object
     *                          might already be the original bean, if the persistence api was able to deserialize it. Otherwise,
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

        final Optional<BeanConverterExtension> applicableExtension = findApplicableExtension(originalBeanClass, parameterizedType);
        if (applicableExtension.isPresent()) {
            T revertedByExtension = applicableExtension.get().revert(persistentBean, originalBeanClass, parameterizedType, this);

            if (revertedByExtension != null
                    && !originalBeanClass.isAssignableFrom(revertedByExtension.getClass())
                    && !(ReflectionTools.isPrimitiveOrWrapper(originalBeanClass) && ReflectionTools.isPrimitiveOrWrapper(revertedByExtension.getClass()))) {
                throw new PersistenceException("The %s#revert() returned an object of type %s, but a type of %s, or one of its subtypes, was requested.",
                        applicableExtension.get().getClass().getName(),
                        revertedByExtension.getClass().getName(),
                        originalBeanClass.getName());
            }

            return revertedByExtension;
        }

        if (persistentBean instanceof PersistentBean) {
            return revertPersistentBean((PersistentBean) persistentBean, originalBeanClass);
        }

        if (!originalBeanClass.isAssignableFrom(persistentBean.getClass())
                && !(ReflectionTools.isPrimitiveOrWrapper(originalBeanClass) && ReflectionTools.isPrimitiveOrWrapper(persistentBean.getClass()))) {
            throw new PersistenceException("An object of type %s has been deserialized, but the type %s, or one of its subtypes, was requested." +
                    "\n%1$s.toString() = \"%s\"",
                    persistentBean.getClass().getName(),
                    originalBeanClass.getName(),
                    persistentBean.toString());
        }

        return (T) persistentBean;
    }


    /**
     * Converts an original bean to the abstract model (most likely {@link PersistentBean}) that is used to save the original bean to e.g. JSON.
     * <p>
     * If more than one applicable {@link BeanConverterExtension} is found, the last registered one will be used.
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

        if (originalBean.getClass().isArray()) {
            return (T) convertObjectArray((Object[]) originalBean);
        }


        final Optional<BeanConverterExtension> applicableExtension = findApplicableExtension(originalBean.getClass(), parameterizedReturnType);
        if (applicableExtension.isPresent()) {
            return (T) applicableExtension.get().convert(originalBean, parameterizedReturnType, this);
        }

        if (originalBean.getClass().equals(type)
                || (parameterizedReturnType != null && originalBean.getClass().equals(parameterizedReturnType.getRawType()))) {
            return (T) convertToPersistentBean(originalBean);
        }

        return (T) convertToPolymorphicPersistentBean(originalBean.getClass().getTypeName(), originalBean);
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

    private <T> T instantiateUsingMatchingConstructor(final Class<T> type, final PersistentBean persistentBean, final Map<Field, String> fields) {
        try {
            return createInstance(type, persistentBean, fields);

        } catch (final NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new PersistenceException("The type %s includes at least one final field. Therefore we tried to automatically detect a " +
                    "constructor accepting all field values, but weren't able to find any. If you still want to transform the bean you " +
                    "have to implement a %s which is able to construct the desired type %s.", e, type, StandardBeanConverterExtension.class.getName(), type);
        }
    }

    private <T> T createInstance(final Class<T> type, final PersistentBean persistentBean, final Map<Field, String> fields) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Class<?>[] parameterTypes = fields.keySet()
                .stream()
                .map(Field::getType)
                .toArray(Class[]::new);

        final List<Object> values = createValuesForConstructingInstance(persistentBean, fields);
        return type.getDeclaredConstructor(parameterTypes).newInstance(values.toArray());
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
            lookedUpValueInBean = revert(lookedUpValueInBean, field.getType(), null);

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


    /**
     * Searches the registered {@link BeanConverterExtension}s for any applicable extensions. A {@link BeanConverterExtension} is applicable
     * if  {@link BeanConverterExtension#isProcessable(Class, ParameterizedType)} returns true.
     * <p>
     * If more than one applicable extension is found, the last registered one will be used.
     *
     * @param beanClass         the class for which a {@link BeanConverterExtension} is wanted
     * @param parameterizedType If beanClass is generic, a parameterized type may be passed to the registered extensions. Otherwise, it is null.
     * @return Optional.empty() if no extension was found. Otherwise, the extension is returned.
     */
    private Optional<BeanConverterExtension> findApplicableExtension(final Class<?> beanClass, final ParameterizedType parameterizedType) {
        List<BeanConverterExtension> allApplicableExtensions = beanConverterExtensions.stream()
                .filter(ext -> ext.isProcessable(beanClass, parameterizedType))
                .collect(Collectors.toList());

        // The list of extensions is a LIFO, so that last added extensions can overwrite previously added extensions.
        // Otherwise, default extensions could not be overwritten by users.
        return allApplicableExtensions.isEmpty()
                ? Optional.empty()
                : Optional.of(allApplicableExtensions.get(allApplicableExtensions.size() - 1));
    }

    private boolean isTransformationNotNecessary(final Object obj, final ParameterizedType parameterizedType) {
        return obj == null
                || obj.getClass().isEnum()
                || ReflectionTools.isPrimitiveOrWrapper(obj.getClass())
                || (!ReflectionTools.isObjectArray(obj.getClass()) && obj.getClass().isArray())
                || findApplicableExtension(obj.getClass(), parameterizedType)
                .map(ext -> ext.skip(obj.getClass(), parameterizedType))
                .orElse(false);
    }


}