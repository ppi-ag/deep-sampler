package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.ReflectionTools;

import java.lang.reflect.Type;
import java.util.Map;

public class MapExtension extends StandardBeanConverterExtension {

    @Override
    public boolean isProcessable(Type beanType) {
        return Map.class.isAssignableFrom(ReflectionTools.getClass(beanType));
    }

    @Override
    public boolean skip(Type beanType) {
        return isProcessable(beanType);
    }
}
