package org.deepsampler.persistence.json.model;

import org.deepsampler.persistence.bean.PersistentBean;
import org.deepsampler.persistence.model.PersistentParameter;

import java.util.List;

public class JsonPersistentParameter implements PersistentParameter {
    private List<PersistentBean> args;

    public JsonPersistentParameter() {
        // DEFAULT CONST FOR JSON SER/DES
    }

    public JsonPersistentParameter(List<PersistentBean> args) {
        this.args = args;
    }

    @Override
    public List<PersistentBean> getParameter() {
        return args;
    }

    public void setParameter(List<PersistentBean> args) {
        this.args = args;
    }
}
