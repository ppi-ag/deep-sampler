package org.deepsampler.persistence.json.model;

import org.deepsampler.persistence.bean.Bean;
import org.deepsampler.persistence.model.PersistentReturnValue;

public class JsonPersistentReturnValue implements PersistentReturnValue {
    private Bean returnValue;

    public JsonPersistentReturnValue() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentReturnValue(Bean returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public Bean getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Bean returnValue) {
        this.returnValue = returnValue;
    }
}
