package org.deepsampler.persistence.json;

public class PersistentSampler {

    private PersistentSampler() {
        //This class is not intended to be instantiated.
    }

    public static PersistentSampleManager source(final SourceManager sourceManager) {
        return new PersistentSampleManager(sourceManager);
    }
}
