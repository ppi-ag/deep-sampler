package org.deepmock.persistence.json.model;

import org.deepmock.persistence.bean.Bean;
import org.deepmock.persistence.model.PersistentReturnValue;

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
