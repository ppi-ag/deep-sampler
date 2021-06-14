package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.ReflectionTools;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

public class MapExtension extends StandardBeanConverterExtension {

    @Override
    public boolean isProcessable(Class<?> beanClass, ParameterizedType beanType) {
        return Map.class.isAssignableFrom(ReflectionTools.getClass(beanClass));
    }

    @Override
    public boolean skip(Class<?> beanClass, ParameterizedType beanType) {
        return isProcessable(beanClass, beanType);
    }
}
