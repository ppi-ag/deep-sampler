package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.bean.ReflectionTools;
import de.ppi.deepsampler.persistence.error.PersistenceException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * A {@link BeanConverterExtension} that is able to convert {@link Collection}s from the original objects to the generic persistent
 * model {@link de.ppi.deepsampler.persistence.model.PersistentBean} and vice versa.
 * <p>
 * Only {@link java.util.Collections}s with elements of non primitive types (i.e. their wrapper) are processed by this extension, since
 * the underlying persistence api is expected to be fully capable of dealing with simple Collections that contain only primitive values.
 * <p>
 * The original type of the Collection will we preserved in most cases. If this is not possible, the original Collection is replaced by
 * an {@link ArrayList} or a {@link java.util.HashSet} depending on the original Collection. E.G. this happens for Lists that have been
 * created by {@link Collections#unmodifiableCollection(Collection)} or {@link Arrays#asList(Object[])}.
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
        if (!(originalBean instanceof Collection)) {
            throw new PersistenceException("The type %s is not a Collection but we tried to apply the %s on it.",
                    originalBean.getClass().getName(),
                    getClass().getName());
        }

        Collection<Object> convertedCollection;

        try {
            convertedCollection = instantiateCollection(originalBean);
        } catch (PersistenceException e) {
            // isProcessable() should limit this extension to objects that implement the Collection interface.
            // In some special cases the concrete Collection cannot be instantiated without complex problems.
            // E.G. this is the case if originalBean has been created by Collections.unmodifiableCollection() or Arrays.asList(..).
            // Both methods return objects of inner classes that are invisible (private class) or don't have a default constructor,
            // so there is no simple way to instantiate them here. In cases like this, we fall back to a normal ArrayList, even though
            // this changes the persisted Bean.

            if (originalBean instanceof Set) {
                convertedCollection = new HashSet<>();
            } else {
                convertedCollection = new ArrayList<>();
            }
        }

        ((Collection<?>) originalBean).stream()
                .map(persistentBeanConverter::convert)
                .forEach(convertedCollection::add);

        return convertedCollection;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T
    revert(Object persistentBean, Type targetBeanType, PersistentBeanConverter persistentBeanConverter) {
        Type[] genericParameterTypes = ((ParameterizedType) targetBeanType).getActualTypeArguments();

        if (genericParameterTypes.length != 1) {
            throw new PersistenceException("%s is only able to deserialize to Collection<T>. But we try to deserialize %s",
                    this.getClass().getSimpleName(),
                    targetBeanType.getTypeName());
        }

        Class<T> collectionElementClass = (Class<T>) genericParameterTypes[0];

        Collection<Object> originalCollection = instantiateCollection(persistentBean);

        ((Collection<Object>) persistentBean).stream()
                .map(o -> persistentBeanConverter.revert(o, collectionElementClass))
                .forEach(originalCollection::add);

        return (T) originalCollection;
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> instantiateCollection(Object persistentBean) {
        Constructor<Collection<Object>> defaultConstructor;

        try {
            defaultConstructor = (Constructor<Collection<Object>>) persistentBean.getClass().getConstructor();
            return defaultConstructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PersistenceException("The targetType %s cannot be instantiated.", e, persistentBean.getClass());
        }
    }
}
