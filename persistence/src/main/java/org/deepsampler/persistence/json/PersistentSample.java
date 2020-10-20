package org.deepsampler.persistence.json;

public class PersistentSample {

    public static PersistentSampleLoader source(final SourceManager sourceManager) {
        return new PersistentSampleLoader(sourceManager);
    }
}
