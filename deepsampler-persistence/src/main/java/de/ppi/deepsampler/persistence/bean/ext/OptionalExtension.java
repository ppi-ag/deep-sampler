/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * A {@link BeanConverterExtension} that is able to record and load {@link Optional} values. The {@link Optional} itself
 * will be ignored, only the value is recorded. An empty {@link Optional} is treated like null.
 *
 */
public class OptionalExtension extends StandardBeanConverterExtension {

    @Override
    public boolean isProcessable(Class<?> beanClass, ParameterizedType beanType) {
        return Optional.class.isAssignableFrom(beanClass);
    }

    @Override
    public Object convert(Object originalBean, ParameterizedType beanType,
                          PersistentBeanConverter persistentBeanConverter) {
        Optional<?> optional = (Optional<?>) originalBean;
        return optional.orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T revert(Object persistentBean, Class<T> targetClass, ParameterizedType type,
                        PersistentBeanConverter persistentBeanConverter) {
        return (T) Optional.ofNullable(persistentBean);
    }

}
