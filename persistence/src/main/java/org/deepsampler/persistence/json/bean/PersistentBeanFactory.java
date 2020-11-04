package org.deepsampler.persistence.json.bean;

import org.deepsampler.persistence.json.bean.ext.BeanFactoryExtension;
import org.deepsampler.persistence.json.error.PersistenceException;
import org.deepsampler.persistence.json.model.PersistentBean;
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
    private Class[] parameterTypes;

    public void addExtension(BeanFactoryExtension extension) {
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
                                                      Map<Field, String> fields) {
        try {
            return createInstance(type, persistentBean, fields);

        } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new PersistenceException("While the type %s has final fields, it was " +
                    "tried to use a matching constructor for all field-values. Because this" +
                    "was not possbile, please provide a BeanFactoryExtension.", e, type);
        }
    }

    private <T> T createInstance(Class<T> type, PersistentBean persistentBean, Map<Field, String> fields) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        parameterTypes = fields.keySet()
                .stream()
                .map(Field::getType)
                .toArray(Class[]::new);

        List<Object> values = createValuesForConstructingInstance(persistentBean, fields);
        return type.getDeclaredConstructor(parameterTypes)
                .newInstance(values.toArray());
    }

    private List<Object> createValuesForConstructingInstance(PersistentBean persistentBean, Map<Field, String> fields) {
        List<Object> values = new ArrayList<>();

        for (final Map.Entry<Field, String> entry : fields.entrySet()) {
            final Field field = entry.getKey();
            final String key = entry.getValue();

            Object lookedUpValueInBean = persistentBean.getValue(key);
            if (lookedUpValueInBean != null) {
                if (lookedUpValueInBean instanceof DefaultPersistentBean) {
                    lookedUpValueInBean = createValueFromPersistentBean((DefaultPersistentBean) lookedUpValueInBean, field.getDeclaringClass());
                }
            }
            values.add(lookedUpValueInBean);
        }
        return values;
    }

    private boolean hasFinalFields(Map<Field, String> fields) {
        return fields.entrySet()
                .stream()
                .anyMatch(entry -> Modifier.isFinal(entry.getKey().getModifiers()));
    }

    private <T> void transferFromBean(final PersistentBean persistentBean, final T instance, final Field field, final String key) {
        Object lookedUpValueInBean = persistentBean.getValue(key);
        if (lookedUpValueInBean != null) {
            if (lookedUpValueInBean instanceof DefaultPersistentBean) {
                lookedUpValueInBean = createValueFromPersistentBean((DefaultPersistentBean) lookedUpValueInBean, field.getDeclaringClass());
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
        List<BeanFactoryExtension> applicableExtensions = findApplicableExtensions(obj.getClass());
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
                    fieldValue = toBean((Object[]) fieldValue);
                } else if (!isPrimitive(field.getType()) && !field.getType().isArray()) {
                    fieldValue = toBean(fieldValue);
                }
            }
            valuesForBean.put(keyForField, fieldValue);
        }

        return new DefaultPersistentBean(valuesForBean);
    }

    private void setValue(final Object obj, final Field field, final Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

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
                || cls == String[].class);
    }

    private boolean isPrimitive(final Class<?> cls) {
        return cls.isPrimitive()
                || cls == Integer.class
                || cls == Boolean.class
                || cls == Byte.class
                || cls == Short.class
                || cls == Long.class
                || cls == String.class;
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

    private List<BeanFactoryExtension> findApplicableExtensions(Class<?> cls) {
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

    public PersistentBean[] toBean(final Object[] objects) {
        final PersistentBean[] persistentBeans = new DefaultPersistentBean[objects.length];
        for (int i = 0; i < objects.length; ++i) {
            persistentBeans[i] = toBean(objects[i]);
        }
        return persistentBeans;
    }

}