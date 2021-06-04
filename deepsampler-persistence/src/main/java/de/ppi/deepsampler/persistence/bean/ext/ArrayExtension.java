package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;
import de.ppi.deepsampler.persistence.error.PersistenceException;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This {@link BeanFactoryExtension} handles the conversion of Arrays from the original Arrays to the generic
 * {@link de.ppi.deepsampler.persistence.model.PersistentBean} and vice versa.
 *
 * This is a default {@link BeanFactoryExtension} which is always active.
 *
 */
public class ArrayExtension extends StandardBeanFactoryExtension {

    @Override
    public boolean isProcessable(Type beanType) {
        if (beanType instanceof Class) {
            return ((Class<?>) beanType).isArray();
        }

        return false;
    }

    /**
     * We skip the conversion of all Arrays, which have primitive (or their wrapper) component types. Primitve arrays
     * will then be handled by the concrete persistence api (i.e. Jackson for JSON serialisation).
     *
     * @param beanType the {@link Type} of the array.
     * @return true if beanType is a primitive array.
     */
    @Override
    public boolean skip(Type beanType) {
        return PersistentBeanFactory.isPrimitiveOrWrapperArray((Class<?>) beanType);
    }


    @Override
    public Object toBean(Object targetBean, Type targetBeanType) {
        PersistentBeanFactory beanFactory = new PersistentBeanFactory();
        Type componentType = ((Class<?>) targetBeanType).getComponentType();

        return Stream.of(targetBean)
                .map(entry -> beanFactory.toBean(entry, componentType))
                .collect(Collectors.toList())
                .toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T ofBean(Object persistentBean, Type targetBeanType) {
        PersistentBeanFactory beanFactory = new PersistentBeanFactory();

        Class<?> arrayType = ((Class<?>) targetBeanType).getComponentType();

        return (T) ((List<Object>) persistentBean).stream()
                .map(o -> beanFactory.convertValueFromPersistentBeanIfNecessary(o, arrayType))
                .collect(Collectors.toList())
                .toArray();
    }
}
