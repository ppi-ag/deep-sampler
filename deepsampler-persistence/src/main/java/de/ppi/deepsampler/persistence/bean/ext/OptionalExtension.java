/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.DefaultPersistentBean;
import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.bean.ReflectionTools;
import de.ppi.deepsampler.persistence.model.PersistentBean;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * A {@link BeanConverterExtension} that is able to record and load {@link Optional} values.
 * The {@link Optional} is encapsulated in a {@link DefaultPersistentBean}, because the {@link PersistentBeanConverter}
 * does not pass null values to {@link BeanConverterExtension}s during deserialization. Instead, null values are returned
 * as the deserialized value without further conversions. This is a design decision that is based on the assumption that
 * null values usually don't need to be converted in most cases.
 * <p>
 * {@link OptionalExtension} is capable of de-/serializing recursively, so that {@link Optional}s value will also be
 * converted, if necessary.
 */
public class OptionalExtension extends StandardBeanConverterExtension {

    public static final String OPTIONAL_PROPERTY = "optionalValue";

    @Override
    public boolean isProcessable(Class<?> beanClass, ParameterizedType beanType) {
        return Optional.class.isAssignableFrom(beanClass);
    }

    @Override
    public Object convert(Object originalBean, ParameterizedType beanType,
                          PersistentBeanConverter persistentBeanConverter) {
        // since isProcessable makes sure, that originalBean is an Optional, we can safely assume, that beanType exists
        // and has exactly 1 type argument.
        Type optionalsValueType = beanType.getActualTypeArguments()[0];
        Object convertedOptionalValue = ((Optional<?>) originalBean).map(o -> persistentBeanConverter.convert(o, optionalsValueType))
                .orElse(null);

        DefaultPersistentBean persistentBean = new DefaultPersistentBean();
        persistentBean.putValue(OPTIONAL_PROPERTY, convertedOptionalValue);

        return persistentBean;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T revert(Object persistentBean, Class<T> targetClass, ParameterizedType type,
                        PersistentBeanConverter persistentBeanConverter) {
        Object optionalValue = ((PersistentBean) persistentBean).getValue(OPTIONAL_PROPERTY);

        Class<?> optionalsValueType = ReflectionTools.getRawClass(type.getActualTypeArguments()[0]);
        ParameterizedType optionalsValueParameterizedType = ReflectionTools.getParameterizedType(type.getActualTypeArguments()[0]);

        Object revertedValue = persistentBeanConverter.revert(optionalValue, optionalsValueType, optionalsValueParameterizedType);
        return (T) Optional.ofNullable(revertedValue);
    }

}
