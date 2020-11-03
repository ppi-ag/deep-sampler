package org.deepsampler.persistence.model;


import java.util.List;

public interface PersistentActualSample {
    List<PersistentMethodCall> getAllCalls();
}
