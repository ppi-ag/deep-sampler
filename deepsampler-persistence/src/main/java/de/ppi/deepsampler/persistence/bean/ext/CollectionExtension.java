package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.bean.ReflectionTools;
import de.ppi.deepsampler.persistence.error.PersistenceException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A {@link BeanConverterExtension} that is able to convert {@link Collection}s from the original objects to the generic persistent
 * model {@link de.ppi.deepsampler.persistence.model.PersistentBean} and vice versa.
 * <p>
 * Only {@link java.util.Collections}s with elements of non primitive types (i.e. their wrapper) are processed by this extension, since
 * the underlying persistence api is expected to be fully capable of dealing with simple Collections that contain only primitive values.
 * <p>
 * This is a default {@link BeanConverterExtension} that is always enabled.
 */
public class CollectionExtension extends StandardBeanConverterExtension {

    @Override
    public boolean isProcessable(Type beanType) {
        return Collection.class.isAssignableFrom(ReflectionTools.getClass(beanType));
    }

    /**
     * We skip the conversion of all Collections, which have primitive (or their wrapper) generic types. Primitve Collections
     * will then be handled by the concrete persistence api (i.e. Jackson for JSON serialisation).
     *
     * @param beanType the {@link Type} of the bean (Collection is expected here) that is handeld by this {@link BeanConverterExtension}
     * @return true if beanType is a primitive {@link Collection}
     */
    @Override
    public boolean skip(Type beanType) {
        return ReflectionTools.isPrimitiveWrapperCollection(beanType);
    }


    @Override
    public Object convert(Object originalBean, PersistentBeanConverter persistentBeanConverter) {
        return ((Collection<?>) originalBean).stream()
                .map(entry -> persistentBeanConverter.convert(entry))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T revert(Object persistentBean, Type targetBeanType, PersistentBeanConverter persistentBeanConverter) {
        Type[] genericParameterTypes = ((ParameterizedType) targetBeanType).getActualTypeArguments();

        if (genericParameterTypes.length != 1) {
            throw new PersistenceException("%s is only able to deserialize to Collection<T>. But we try to deserialize %s",
                    this.getClass().getSimpleName(),
                    targetBeanType.getTypeName());
        }

        Class<T> collectionElementClass = (Class<T>) genericParameterTypes[0];

        Constructor<Collection<Object>> defaultConstructor;
        Collection<Object> valueListCollection;

        try {
            defaultConstructor = (Constructor<Collection<Object>>) persistentBean.getClass().getConstructor();
            valueListCollection = defaultConstructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PersistenceException("The targetType %s cannot be instantiated.", e, persistentBean.getClass());
        }

        ((Collection<Object>) persistentBean).stream()
                .map(o -> persistentBeanConverter.revert(o, collectionElementClass))
                .forEach(valueListCollection::add);

        return (T) valueListCollection;
    }
}
