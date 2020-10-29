package org.deepsampler.persistence.json;

public class PersistentSample {

    public static PersistentSampleManager source(final SourceManager sourceManager) {
        return new PersistentSampleManager(sourceManager);
    }
}
