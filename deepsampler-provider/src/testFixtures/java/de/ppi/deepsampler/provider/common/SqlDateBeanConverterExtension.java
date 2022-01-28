package de.ppi.deepsampler.provider.common;

import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.bean.ext.StandardBeanConverterExtension;
import de.ppi.deepsampler.persistence.error.PersistenceException;

import java.lang.reflect.ParameterizedType;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * java.sql.Date is by default converted to a simple long value, which represents the epoch value. This Extension
 * converts the Date to a human-readable String.
 */
public class SqlDateBeanConverterExtension extends StandardBeanConverterExtension {

    private final DateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public boolean isProcessable(Class<?> beanClass, ParameterizedType beanType) {
        return beanClass.isAssignableFrom(Date.class);
    }

    @Override
    public Object convert(Object originalBean, ParameterizedType beanType, PersistentBeanConverter persistentBeanConverter) {
        return simpleDateFormat.format((Date) originalBean);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T revert(Object persistentBean, Class<T> targetClass, ParameterizedType type, PersistentBeanConverter persistentBeanConverter) {
        try {
            return (T) new Date(simpleDateFormat.parse((String) persistentBean).getTime());
        } catch (ParseException e) {
            throw new PersistenceException("SqlDate %s could not be deserialized!", e, persistentBean);
        }
    }
}
