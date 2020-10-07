package org.deepsampler.persistence;

public class PersistentSample {

    public static PersistentSampleLoader source(SourceManager sourceManager) {
        return new PersistentSampleLoader(sourceManager);
    }
}
