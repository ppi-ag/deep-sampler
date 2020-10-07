package org.deepsampler.persistence.bean;

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

public class BeanFactory {

    public static <T> T[] ofBean(Bean[] bean, Class<T> cls) {
        T[] instances = (T[]) Array.newInstance(cls, bean.length);
        for (int i = 0; i < bean.length; ++i) {
            instances[i] = ofBean(bean[i], cls);
        }
        return instances;
    }

    public static <T> T ofBean(Bean bean, Class<T> cls) {
        T instance = instantiate(cls);

        Map<Field, String> fields = getAllFields(cls);

        for (Map.Entry<Field, String> entry : fields.entrySet()) {
            Field field = entry.getKey();
            String key = entry.getValue();

            transferFromBean(bean, instance, field, key);
        }
        return instance;
    }

    private static <T> void transferFromBean(Bean bean, T instance, Field field, String key) {
        Object lookedUpValueInBean = bean.getValue(key);
        if (lookedUpValueInBean != null) {
            if (lookedUpValueInBean instanceof Bean) {
                lookedUpValueInBean = ofBean((Bean) lookedUpValueInBean, field.getDeclaringClass());
            }
            setValue(instance, field, lookedUpValueInBean);
        }
    }

    private static <T> T instantiate(Class<T> cls) {
        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator<T> instantiatorOf = objenesis.getInstantiatorOf(cls);
        return instantiatorOf.newInstance();
    }

    public static Bean toBean(Object obj) {

        Map<Field, String> fieldStringMap = getAllFields(obj.getClass());

        Map<String, Object> valuesForBean = new HashMap<>();
        for (Map.Entry<Field, String> entry : fieldStringMap.entrySet()) {
            String keyForField = entry.getValue();
            Object fieldValue = retrieveValue(obj, entry.getKey());

            if (isObjectArray(fieldValue)) {
                fieldValue = toBean((Object[]) fieldValue);
            } else if (!isPrimitive(fieldValue)) {
                fieldValue = toBean(fieldValue);
            }
            valuesForBean.put(keyForField, fieldValue);
        }

        return new Bean(valuesForBean);
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

    private static boolean isObjectArray(Object fieldValue) {
        return fieldValue.getClass().isArray() && !(fieldValue instanceof int[]
                || fieldValue instanceof Integer[]
                || fieldValue instanceof boolean[]
                || fieldValue instanceof Boolean[]
                || fieldValue instanceof byte[]
                || fieldValue instanceof Byte[]
                || fieldValue instanceof short[]
                || fieldValue instanceof Short[]
                || fieldValue instanceof long[]
                || fieldValue instanceof Long[]
                || fieldValue instanceof char[]
                || fieldValue instanceof String[]);
    }

    private static boolean isPrimitive(Object fieldValue) {
        return fieldValue.getClass().isPrimitive()
                || fieldValue instanceof Integer
                || fieldValue instanceof Boolean
                || fieldValue instanceof Byte
                || fieldValue instanceof Short
                || fieldValue instanceof Long
                || fieldValue instanceof String;
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

    public static List<Bean> toBean(List<Object> objectList) {
        return objectList.stream()
                .map(obj -> toBean(obj))
                .collect(Collectors.toList());
    }

    public static Bean[] toBean(Object[] objects) {
        Bean[] beans = new Bean[objects.length];
        for (int i = 0; i < objects.length; ++i) {
            beans[i] = toBean(objects[i]);
        }
        return beans;
    }

}
