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

    @SuppressWarnings("unchecked")
    public static <T> T[] ofBean(PersistentBean[] persistentBean, Class<T> cls) {
        T[] instances = (T[]) Array.newInstance(cls, persistentBean.length);
        for (int i = 0; i < persistentBean.length; ++i) {
            instances[i] = ofBean(persistentBean[i], cls);
        }
        return instances;
    }

    @SuppressWarnings("unchecked")
    public static <T> T ofBeanIfNecessary(Object beanObj, Class<T> cls) {
        if (beanObj instanceof PersistentBean) {
            return ofBean((PersistentBean) beanObj, cls);
        }
        return (T) beanObj;
    }

    public static <T> T ofBean(PersistentBean persistentBean, Class<T> cls) {
        T instance = instantiate(cls);

        Map<Field, String> fields = getAllFields(cls);

        for (Map.Entry<Field, String> entry : fields.entrySet()) {
            Field field = entry.getKey();
            String key = entry.getValue();

            transferFromBean(persistentBean, instance, field, key);
        }
        return instance;
    }

    private static <T> void transferFromBean(PersistentBean persistentBean, T instance, Field field, String key) {
        Object lookedUpValueInBean = persistentBean.getValue(key);
        if (lookedUpValueInBean != null) {
            if (lookedUpValueInBean instanceof DefaultPersistentBean) {
                lookedUpValueInBean = ofBean((DefaultPersistentBean) lookedUpValueInBean, field.getDeclaringClass());
            }
            setValue(instance, field, lookedUpValueInBean);
        }
    }

    private static <T> T instantiate(Class<T> cls) {
        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator<T> instantiatorOf = objenesis.getInstantiatorOf(cls);
        return instantiatorOf.newInstance();
    }

    public static PersistentBean toBean(Object obj) {

        Map<Field, String> fieldStringMap = getAllFields(obj.getClass());

        Map<String, Object> valuesForBean = new HashMap<>();
        for (Map.Entry<Field, String> entry : fieldStringMap.entrySet()) {
            String keyForField = entry.getValue();
            Field field = entry.getKey();
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

    private static void setValue(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Object retrieveValue(Object obj, Field field) {
        Object fieldValue;
        try {
            field.setAccessible(true);
            fieldValue = field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        return fieldValue;
    }

    private static boolean isObjectArray(Class<?> cls) {
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

    private static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive()
                || cls == Integer.class
                || cls == Boolean.class
                || cls == Byte.class
                || cls == Short.class
                || cls == Long.class
                || cls == String.class;
    }

    private static Map<Field, String> getAllFields(Class<?> cls) {
        Map<Field, String> fields = new HashMap<>();
        Class<?> currentCls = cls;
        int depth = 0;
        while (currentCls != null) {
            for (Field field : currentCls.getDeclaredFields()) {
                if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
                    fields.put(field, String.format("%s$%s", depth, field.getName()));
                }
            }
            depth += 1;
            currentCls = currentCls.getSuperclass();
        }
        return fields;
    }

    public static Object toBeanIfNecessary(Object obj) {
        return transformationNotNecessary(obj) ? obj : toBean(obj);
    }

    private static boolean transformationNotNecessary(Object obj) {
        return isPrimitive(obj.getClass()) || (!isObjectArray(obj.getClass()) && obj.getClass().isArray());
    }

    public static List<Object> toBeanIfNecessary(List<Object> objectList) {
        return objectList.stream()
                .map(PersistentBeanFactory::toBeanIfNecessary)
                .collect(Collectors.toList());
    }

    public static PersistentBean[] toBean(Object[] objects) {
        PersistentBean[] persistentBeans = new DefaultPersistentBean[objects.length];
        for (int i = 0; i < objects.length; ++i) {
            persistentBeans[i] = toBean(objects[i]);
        }
        return persistentBeans;
    }

}
