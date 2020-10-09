package org.deepsampler.persistence.json.bean;

import org.deepsampler.persistence.json.model.PersistentBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultPersistentBean implements PersistentBean {

    private Map<String, Object> values;

    public DefaultPersistentBean() {
        values = new HashMap<>();
    }

    public DefaultPersistentBean(Map<String, Object> values) {
        this.values = new HashMap<>(values);
    }

    public void setValues(Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    public Object getValue(String key) {
        return values.get(key);
    }
}
