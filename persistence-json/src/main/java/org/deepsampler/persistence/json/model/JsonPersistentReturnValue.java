package org.deepsampler.persistence.json.model;

import org.deepsampler.persistence.bean.PersistentBean;
import org.deepsampler.persistence.model.PersistentReturnValue;

public class JsonPersistentReturnValue implements PersistentReturnValue {
    private PersistentBean returnValue;

    public JsonPersistentReturnValue() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentReturnValue(PersistentBean returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public PersistentBean getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(PersistentBean returnValue) {
        this.returnValue = returnValue;
    }
}
