package org.deepsampler.persistence;

public class PersistentPersonality {

    public static PersistentPersonalityLoader source(SourceManager sourceManager) {
        return new PersistentPersonalityLoader(sourceManager);
    }
}
