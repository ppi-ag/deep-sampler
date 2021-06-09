package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.model.PersistentBean;

import java.lang.reflect.*;
import java.util.Map;

/**
 * This {@link BeanFactoryExtension} is able to map Maps from the original Objects to the generic model ({@link PersistentBean}.
 * It is specialized in Maps that comply to Map<String, ? extends Object>. This is a special case because Maps with Strings as keys can be
 * serialized in JSON in a natural way, by using the String keys as property names in JSON.
 *
 * This is a default {@link BeanFactoryExtension} which is always active.
 */
public class MapWithStringKeyExtension extends StandardBeanFactoryExtension {

    @Override
    public boolean isProcessable(Type targetType) {
        if (targetType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) targetType;

            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            return Map.class.isAssignableFrom(rawType) && typeArguments[0] == String.class;
        }

        return false;
    }

    @Override
    public boolean skip(Type beanType) {
        return PersistentBeanFactory.isPrimitiveOrWrapperMap(beanType);
    }

    @Override
    public Object toBean(Object targetBean, Type targetBeanType) {
        PersistentBeanFactory beanFactory = new PersistentBeanFactory();
        Type[] parameters = ((ParameterizedType) targetBeanType).getActualTypeArguments();
        Type keyType = parameters[0];
        Type valueType = parameters[1];

        Map<String, Object> persistentMap = instantiateMap(targetBean.getClass());

        ((Map<String, Object>) targetBean).entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object value = beanFactory.toBean(entry.getValue(), valueType);

                    return new Object[]{key, value};
                })
                .forEach(pair -> persistentMap.put((String) pair[0], pair[1]));


        return persistentMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T ofBean(Object persistentBean, Type targetBeanType) {
        PersistentBeanFactory beanFactory = new PersistentBeanFactory();

        Type[] genericParameterTypes = ((ParameterizedType) targetBeanType).getActualTypeArguments();

        if (genericParameterTypes.length != 2) {
            throw new PersistenceException("MapWithStringKeyExtension is only able to deserialize to Map<K, V>. But we try to deserialize %s", targetBeanType.getTypeName());
        }

        Class<T> keyClass = (Class<T>) genericParameterTypes[0];
        Class<T> valueClass = (Class<T>) genericParameterTypes[1];

        if (keyClass != String.class) {
            throw new PersistenceException("This extension is only able to work with Maps that have a String-key, but we received a %s.", targetBeanType.getTypeName());
        }

        Map<String, Object> valueMap = instantiateMap(persistentBean.getClass());

        ((Map<String, Object>) persistentBean).entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object value = beanFactory.convertValueFromPersistentBeanIfNecessary(entry.getValue(), valueClass);
                    return new Object[]{key, value};
                })
                .forEach(pair -> valueMap.put((String) pair[0], pair[1]));

        return (T) valueMap;
    }

    private Map<String, Object> instantiateMap(Class<?> cls) {
        try {
            Constructor<Map<String, Object>> defaultConstructor = (Constructor<Map<String, Object>>) cls.getConstructor();
            return defaultConstructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PersistenceException("The targetType %s cannot be instantiated.", e, cls);
        }
    }
}
