package org.deepsampler.persistence.json.bean;

import org.deepsampler.persistence.json.model.PersistentBean;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersistentBeanFactory {

    private PersistentBeanFactory() {
        // This class is not intended to be instantiated.
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] ofBean(final PersistentBean[] persistentBean, final Class<T> cls) {
        final T[] instances = (T[]) Array.newInstance(cls, persistentBean.length);
        for (int i = 0; i < persistentBean.length; ++i) {
            instances[i] = createValueFromPersistentBean(persistentBean[i], cls);
        }
        return instances;
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertValueFromPersistentBeanIfNecessary(final Object value, final Class<T> type) {
        if (value instanceof PersistentBean) {
            return createValueFromPersistentBean((PersistentBean) value, type);
        }
        return (T) value;
    }

    public static <T> T createValueFromPersistentBean(final PersistentBean value, final Class<T> type) {
        final T instance = instantiate(type);

        final Map<Field, String> fields = getAllFields(type);

        for (final Map.Entry<Field, String> entry : fields.entrySet()) {
            final Field field = entry.getKey();
            final String key = entry.getValue();

            transferFromBean(value, instance, field, key);
        }
        return instance;
    }

    private static <T> void transferFromBean(final PersistentBean persistentBean, final T instance, final Field field, final String key) {
        Object lookedUpValueInBean = persistentBean.getValue(key);
        if (lookedUpValueInBean != null) {
            if (lookedUpValueInBean instanceof DefaultPersistentBean) {
                lookedUpValueInBean = createValueFromPersistentBean((DefaultPersistentBean) lookedUpValueInBean, field.getDeclaringClass());
            }
            setValue(instance, field, lookedUpValueInBean);
        }
    }

    private static <T> T instantiate(final Class<T> cls) {
        final Objenesis objenesis = new ObjenesisStd();
        final ObjectInstantiator<T> instantiatorOf = objenesis.getInstantiatorOf(cls);
        return instantiatorOf.newInstance();
    }

    public static PersistentBean toBean(final Object obj) {

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

    private static void setValue(final Object obj, final Field field, final Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Object retrieveValue(final Object obj, final Field field) {
        final Object fieldValue;
        try {
            field.setAccessible(true);
            fieldValue = field.get(obj);
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        return fieldValue;
    }

    private static boolean isObjectArray(final Class<?> cls) {
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

    private static boolean isPrimitive(final Class<?> cls) {
        return cls.isPrimitive()
                || cls == Integer.class
                || cls == Boolean.class
                || cls == Byte.class
                || cls == Short.class
                || cls == Long.class
                || cls == String.class;
    }

    private static Map<Field, String> getAllFields(final Class<?> cls) {
        final Map<Field, String> fields = new HashMap<>();
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

    public static Object toBeanIfNecessary(final Object obj) {
        return isTransformationNotNecessary(obj) ? obj : toBean(obj);
    }

    private static boolean isTransformationNotNecessary(final Object obj) {
        return obj == null || isPrimitive(obj.getClass()) || (!isObjectArray(obj.getClass()) && obj.getClass().isArray());
    }

    public static List<Object> toBeanIfNecessary(final List<Object> objectList) {
        return objectList.stream()
                .map(PersistentBeanFactory::toBeanIfNecessary)
                .collect(Collectors.toList());
    }

    public static PersistentBean[] toBean(final Object[] objects) {
        final PersistentBean[] persistentBeans = new DefaultPersistentBean[objects.length];
        for (int i = 0; i < objects.length; ++i) {
            persistentBeans[i] = toBean(objects[i]);
        }
        return persistentBeans;
    }

}