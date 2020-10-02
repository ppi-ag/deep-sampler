package org.deepmock.persistence.bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Bean {
    private Map<String, Object> values;

    public Bean() {
        values = new HashMap<>();
    }

    public Bean(Map<String, Object> values) {
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
