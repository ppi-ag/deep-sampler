package org.deepsampler.persistence.json.model;


import java.util.List;

public interface PersistentActualSample {
    List<PersistentMethodCall> getAllCalls();
}
