package org.deepsampler.persistence.json;

public class PersistentSample {

    private PersistentSample() {
        //This class is not intended to be instantiated.
    }

    public static PersistentSampleLoader source(final SourceManager sourceManager) {
        return new PersistentSampleLoader(sourceManager);
    }
}
