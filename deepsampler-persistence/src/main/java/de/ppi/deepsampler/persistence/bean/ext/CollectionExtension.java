package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;
import de.ppi.deepsampler.persistence.error.PersistenceException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A {@link BeanFactoryExtension} that is able to convert Collections from the original objects to the generic persistent
 * model {@link de.ppi.deepsampler.persistence.model.PersistentBean} and vice versa.
 *
 * This is a default {@link BeanFactoryExtension} that is always enabled.
 */
public class CollectionExtension extends StandardBeanFactoryExtension {

    @Override
    public boolean isProcessable(Type beanType) {
        if (beanType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) beanType;

            return Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType());
        }

        return false;
    }

    /**
     * We skip the conversion of all Collections, which have primitive (or their wrapper) generic types. Primitve Collections
     * will then be handled by the concrete persistence api (i.e. Jackson for JSON serialisation).
     *
     * @param beanType the {@link Type} of the bean (Collection is expected here) that is handeld by this {@link BeanFactoryExtension}
     * @return true if beanType is a primitive {@link Collection}
     */
    @Override
    public boolean skip(Type beanType) {
        return PersistentBeanFactory.isPrimitiveOrWrapperList(beanType);
    }


    @Override
    public Object toBean(Object targetBean, Type targetBeanType) {
        PersistentBeanFactory beanFactory = new PersistentBeanFactory();
        Type parameterType = ((ParameterizedType) targetBeanType).getActualTypeArguments()[0];

        return ((Collection<?>) targetBean).stream()
                .map(entry -> beanFactory.toBean(entry, parameterType))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T ofBean(Object persistentBean, Type targetBeanType) {
        PersistentBeanFactory beanFactory = new PersistentBeanFactory();

        Type[] genericParameterTypes = ((ParameterizedType) targetBeanType).getActualTypeArguments();

        if (genericParameterTypes.length != 1) {
            throw new PersistenceException("ListExtension is only able to deserialize to List<T>. But we try to deserialize %s", targetBeanType.getTypeName());
        }

        Class<T> listElementClass = (Class<T>) genericParameterTypes[0];

        Constructor<Collection<Object>> defaultConstructor;
        Collection<Object> valueListCollection;

        try {
            defaultConstructor = (Constructor<Collection<Object>>) persistentBean.getClass().getConstructor();
            valueListCollection = defaultConstructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PersistenceException("The targetType %s cannot be instantiated.", e, persistentBean.getClass());
        }

        ((Collection<Object>) persistentBean).stream()
                .map(o -> beanFactory.convertValueFromPersistentBeanIfNecessary(o, listElementClass))
                .forEach(valueListCollection::add);

        return (T) valueListCollection;
    }
}
