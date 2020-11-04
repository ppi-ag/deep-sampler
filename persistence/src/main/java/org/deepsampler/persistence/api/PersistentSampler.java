package org.deepsampler.persistence.api;

/**
 * The persistent sampler is the entry point for all operations regarding the persistence:
 * <ul>
 *     <li>loading samples</li>
 *     <li>recording samples</li>
 * </ul>
 *
 * <p>
 * To use the persistent you need a {@link SourceManager}, which will determine in which way
 * the samples have to be recorded/loaded.
 * </p>
 * <p>
 * If this prerequisite is fulfilled you can use {{@link PersistentSampler#source(SourceManager)}} to
 * start the definition of the persistent sample.
 * </p>
 *
 * <p>
 *     Example using a json-SourceManager:<br><br>
 *     <code>
 *         PersistentSampleManager.source(JsonSourceManager.builder("file.json")).record();
 *     </code><br>
 *     Its also possible to define multiple sources:
 *     <code>
 *         PersistentSampleManager.source(JsonSourceManager.builder("file.json"))
 *              .source(JsonSourceManager.builder("file-copy.json"))
 *              .record();
 *     </code>
 * </p>
 *
 * Currently there is one SourceManager: JsonSourceManager (you will need to include persistence-json as maven dependency).
 * If you need to write/load your samples to other data-sources you cant implement your own {@link SourceManager}.
 */
public class PersistentSampler {

    private PersistentSampler() {
        //This class is not intended to be instantiated.
    }

    /**
     * Entry method to load/record some samples. You have to build a {@link SourceManager}, responsible
     * for interacting with the persistent data source (file, database, whatever you like=.
     *
     * @param sourceManager the {@link SourceManager}
     * @return
     */
    public static PersistentSampleManager source(final SourceManager sourceManager) {
        return new PersistentSampleManager(sourceManager);
    }
}
