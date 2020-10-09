package org.deepsampler.persistence.json;

public class PersistentSample {

    public static PersistentSampleLoader source(SourceManager sourceManager) {
        return new PersistentSampleLoader(sourceManager);
    }
}
