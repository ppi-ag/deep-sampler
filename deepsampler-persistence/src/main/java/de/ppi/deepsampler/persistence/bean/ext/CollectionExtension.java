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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link BeanConverterExtension} that is able to convert {@link Collection}s from the original objects to the generic persistent
 * model {@link de.ppi.deepsampler.persistence.model.PersistentBean} and vice versa.
 * <p>
 * Only {@link java.util.Collections}s with elements of non-primitive types (i.e. their wrapper) are processed by this extension, since
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
    public boolean isProcessable(Class<?> beanClass, ParameterizedType parameterizedType) {
        return Collection.class.isAssignableFrom(beanClass);
    }

    /**
     * We skip the conversion of all Collections, which have primitive (or their wrapper) generic types. Primitive Collections
     * will then be handled by the concrete persistence api (i.e. Jackson for JSON serialisation).
     *
     * @param beanType the {@link Type} of the bean (Collection is expected here) that is handled by this {@link BeanConverterExtension}
     * @return true if beanType is a primitive {@link Collection}
     */
    @Override
    public boolean skip(Class<?> beanClass, ParameterizedType beanType) {
        return ReflectionTools.hasPrimitiveTypeParameters(beanType);
    }

    @Override
    public Object convert(Object originalBean, ParameterizedType parameterizedType, PersistentBeanConverter persistentBeanConverter) {
        if (!(originalBean instanceof Collection)) {
            throw new PersistenceException("The type %s is not a Collection but we tried to apply the %s on it.",
                    originalBean.getClass().getName(),
                    getClass().getName());
        }

        Type entryType = getCollectionsEntryType(originalBean.getClass(), parameterizedType);
        Collection<Object> convertedCollection = getNewCollection(originalBean.getClass());

        ((Collection<?>) originalBean).stream()
                .map(entry -> persistentBeanConverter.convert(entry, entryType))
                .forEach(convertedCollection::add);

        return convertedCollection;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T revert(Object persistentBean, Class<T> targetBeanClass, ParameterizedType targetBeanType, PersistentBeanConverter persistentBeanConverter) {
        Type entryType = getCollectionsEntryType(targetBeanClass, targetBeanType);

        ParameterizedType collectionElementType;
        Class<?> collectionElementClass;
        if (entryType instanceof ParameterizedType) {
            collectionElementType = (ParameterizedType) entryType;
            collectionElementClass = (Class<?>) collectionElementType.getRawType();
        } else {
            collectionElementType = null;
            collectionElementClass = (Class<?>) entryType;
        }

        Collection<Object> originalCollection = getNewCollection(persistentBean.getClass());

        ((Collection<Object>) persistentBean).stream()
                .map(o -> persistentBeanConverter.revert(o, collectionElementClass, collectionElementType))
                .forEach(originalCollection::add);

        return (T) originalCollection;
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> getNewCollection(Class<?> persistentBeanClass) {
        try {
            Constructor<Collection<Object>> defaultConstructor = (Constructor<Collection<Object>>) persistentBeanClass.getConstructor();
            return defaultConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // isProcessable() should limit this extension to objects that implement the Collection interface.
            // In some special cases the concrete Collection cannot be instantiated without complex problems.
            // E.G. this is the case if originalBean has been created by Collections.unmodifiableCollection() or Arrays.asList(..).
            // Both methods return objects of inner classes that are invisible (private class) or don't have a default constructor,
            // so there is no simple way to instantiate them here. In cases like this, we fall back to a normal ArrayList, even though
            // this changes the persisted Bean.

            if (Set.class.isAssignableFrom(persistentBeanClass)) {
                return new HashSet<>();
            } else {
                return new ArrayList<>();
            }
        }
    }

    private Type getCollectionsEntryType(Class<?> originalBeanClass, ParameterizedType parameterizedType) {
        if (parameterizedType == null) {
            throw new PersistenceException("%s is only able to serialize subtypes of Collections, that declare exactly one generic type parameter. " +
                    "%s does not have any generic type parameters. " +
                    "The type parameter is necessary to detect the type of the objects inside of the Collection. " +
                    "%s's can be used to tell DeepSampler, how to de/serialize beans, that cannot be serialized by DeepSampler out of the box.",
                    getClass().getSimpleName(), originalBeanClass.getName(), BeanConverterExtension.class.getName());
        }

        if (parameterizedType.getActualTypeArguments().length != 1) {
            throw new PersistenceException("%s is only able to serialize subtypes of Collections, that declare exactly one generic type parameter. " +
                    "%s declares %d type parameters. " +
                    "The type parameter is necessary to detect the type of the objects inside of the Collection. " +
                    "%s's can be used to tell DeepSampler, how to de/serialize beans, that cannot be serialized by DeepSampler out of the box.",
                    getClass().getSimpleName(),
                    originalBeanClass.getName(),
                    parameterizedType.getActualTypeArguments().length,
                    BeanConverterExtension.class.getName());
        }

        return parameterizedType.getActualTypeArguments()[0];
    }
}