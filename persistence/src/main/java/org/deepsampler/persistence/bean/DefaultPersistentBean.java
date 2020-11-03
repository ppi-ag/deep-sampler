package org.deepsampler.persistence.bean;

import org.deepsampler.persistence.model.PersistentBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultPersistentBean implements PersistentBean {

    private Map<String, Object> values;

    public DefaultPersistentBean() {
        values = new HashMap<>();
    }

    public DefaultPersistentBean(final Map<String, Object> values) {
        this.values = new HashMap<>(values);
    }

    @Override
    public void setValues(final Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    @Override
    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public Object getValue(final String key) {
        return values.get(key);
    }
}
