/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.bean.ReflectionTools;
import de.ppi.deepsampler.persistence.error.PersistenceException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

/**
 * This {@link BeanConverterExtension} is able to convert {@link Map}s into a persistable model that can be sent to
 * the underlying persistence api and vice versa.
 * <p>
 * This extension is limited to {@link Map}s that have a primitive key (i.e. wrapper types of primitives, since {@link Map}s can't
 * hold primitives directly)
 * <p>
 * The values of the {@link Map} can either be primitives (i.e. wrappers) or complex objects. In the first case, the values will be
 * sent directly to the persistence api. In the latter case, the values will be converted to {@link de.ppi.deepsampler.persistence.model.PersistentBean}s
 * during the serialization process.
 * <p>
 * The original {@link Map}-types are preserved in most cases. But in some rare cases this is not possible because the original {@link Map}s
 * are not visible, or don't provide a adequate constructor. This is i.e. the case for {@link Map}s that have been created using
 * {@link Collections#unmodifiableMap(Map)}. In cases like this, the original {@link Map} is replaced by a common {@link HashMap}.
 * <p>
 * This is a default extension that is always active.
 */
public class MapPrimitiveKeyExtension extends StandardBeanConverterExtension {

    @Override
    public boolean isProcessable(Class<?> beanClass, ParameterizedType beanType) {
        return Map.class.isAssignableFrom(ReflectionTools.getClass(beanClass))
                && beanType.getActualTypeArguments().length == 2
                && ReflectionTools.hasPrimitiveTypeParameters(beanType);
    }


    @Override
    @SuppressWarnings("unchecked")
    public Object convert(Object originalBean, ParameterizedType beanType, PersistentBeanConverter persistentBeanConverter) {
        if (!(originalBean instanceof Map)) {
            throw new PersistenceException("The type %s is not a Map but we tried to apply the %s on it.",
                    originalBean.getClass().getName(),
                    getClass().getName());
        }

        Map<Object, Object> convertedMap = createNewMap(originalBean.getClass());

        ParameterizedType entryType;

        if (beanType.getActualTypeArguments()[1] instanceof ParameterizedType) {
            entryType = (ParameterizedType) beanType.getActualTypeArguments()[1];
        } else {
            entryType = null;
        }

        for (Entry<Object, Object> entry : ((Map<Object, Object>) originalBean).entrySet()) {
            Object value = persistentBeanConverter.convert(entry.getValue(), entryType);
            String key = entry.getKey() != null ? entry.getKey().toString() : "null";
            convertedMap.put(key, value);
        }

        return convertedMap;
    }

    private Map<Object, Object> createNewMap(Class<?> originalBeanClass) {
        try {
            return instantiateMap(originalBeanClass);
        } catch (PersistenceException e) {
            // isProcessable() should limit this extension to objects that implement the Map interface.
            // In some special cases the concrete Map cannot be instantiated without complex problems.
            // E.G. this is the case if originalBean has been created by Collections.unmodifiableMap().
            // Such methods return objects of inner classes that are invisible (private class) or don't have a default constructor,
            // so there is no simple way to instantiate them here. In cases like this, we fall back to a normal HashMap, even though
            // this changes the persisted Bean.

            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> instantiateMap(Class<?> persistentBeanClass) {
        try {
            Constructor<Map<Object, Object>> defaultConstructor = (Constructor<Map<Object, Object>>) persistentBeanClass.getConstructor();
            return defaultConstructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PersistenceException("The targetType %s cannot be instantiated.", e, persistentBeanClass.getTypeName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T revert(Object persistentBean, Class<T> targetClass, ParameterizedType type, PersistentBeanConverter persistentBeanConverter) {
        Type[] genericParameterTypes = type.getActualTypeArguments();

        if (genericParameterTypes.length != 2) {
            throw new PersistenceException("%s is only able to deserialize to Map<String, V>. But we try to deserialize %s",
                    getClass().getTypeName(), targetClass.getTypeName());
        }

        Class<T> valueClass;
        ParameterizedType valueType;

        if (genericParameterTypes[1] instanceof ParameterizedType) {
            valueType = (ParameterizedType) genericParameterTypes[1];
            valueClass = (Class<T>) valueType.getRawType();
        } else {
            valueType = null;
            valueClass = (Class<T>) genericParameterTypes[1];
        }

        Class<?> keyClass = (Class<T>) genericParameterTypes[0];

        Map<Object, Object> valueMap = instantiateMap(persistentBean.getClass());

        for (Entry<String, Object> entry : ((Map<String, Object>) persistentBean).entrySet()) {
            Object key = ReflectionTools.parseString(entry.getKey(), keyClass);
            Object value = persistentBeanConverter.revert(entry.getValue(), valueClass, valueType);
            valueMap.put(key, value);
        }

        return (T) valueMap;
    }
}
