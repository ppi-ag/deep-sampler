/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;

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
 *     Example of using a json-SourceManager:<br><br>
 *     <code>
 *         PersistentSampleManager.source(JsonSourceManager.builder("file.json")).record();
 *     </code><br>
 *     It's also possible to define multiple sources to record/load to/from multiple targets:
 *     <code>
 *         PersistentSampleManager.source(JsonSourceManager.builder("file.json"))
 *              .source(JsonSourceManager.builder("file-copy.json"))
 *              .record();
 *     </code>
 * </p>
 *
 * With the JsonSourceManager deepsampler provides a reference implementation of a {@link SourceManager} which is capable of writing/loading to/from *.json files. To use it just include the project
 * persistence-json as maven dependency.
 * <br>
 * If you need to write/load your samples to other data-sources you can implement your own {@link SourceManager}.
 */
public class PersistentSampler {

    private PersistentSampler() {
        //This class is not intended to be instantiated.
    }

    /**
     * Entry method to load/record some samples. You have to build a {@link SourceManager}, responsible
     * for interacting with the persistent data source (file, database, whatever you like).
     *
     * @param sourceManager the {@link SourceManager}
     * @return A new {@link PersistentSampleManager}
     */
    public static PersistentSampleManager source(final SourceManager sourceManager) {
        return new PersistentSampleManager(sourceManager);
    }
}
