package de.ppi.deepsampler.persistence.bean.ext;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class CollectionMapExtension extends StandardBeanFactoryExtension {

    @Override
    public boolean isProcessable(Type targetType) {
        return targetType instanceof Class &&
                Collection.class.isAssignableFrom((Class<?>) targetType) ||
                Map.class.isAssignableFrom((Class<?>) targetType);
    }

    @Override
    public boolean skip(Type beanType) {
        return isProcessable(beanType);
    }
}
