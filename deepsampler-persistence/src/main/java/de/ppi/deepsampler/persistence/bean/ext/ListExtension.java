package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.model.Persistable;
import de.ppi.deepsampler.persistence.model.PersistentBean;
import de.ppi.deepsampler.persistence.model.PersistentList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListExtension extends StandardBeanFactoryExtension {
    @Override
    public boolean isProcessable(Class<?> beanCls) {
        return List.class.isAssignableFrom(beanCls);
    }

    @Override
    public PersistentList toBean(Object bean) {
        PersistentBeanFactory beanFactory = new PersistentBeanFactory();

        List<Object> persistableObjects = ((List<?>) bean).stream()
                .map(beanFactory::toBean)
                .collect(Collectors.toList());

        return new PersistentList(persistableObjects);
    }

    @Override
    public <T> T ofBean(Persistable bean, Class<T> cls) {
        PersistentBeanFactory beanFactory = new PersistentBeanFactory();

        Class<?> listElementClass = (Class<?>) ((ParameterizedType) cls.getGenericSuperclass()).getActualTypeArguments()[0];

        try {
            List<Object> list = (List<Object>) cls.getConstructor().newInstance();
            List<?> persistableList = ((PersistentList) bean).getPersistableList();

            persistableList.stream()
                    .map(b -> beanFactory.convertValueFromPersistentBeanIfNecessary(b, listElementClass))
                    .forEach(o -> list.add(o));

            return (T) list;

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |NoSuchMethodException e) {
            throw new PersistenceException("The type %s must provide a default constructor.", e, cls.getName());
        }
    }
}
