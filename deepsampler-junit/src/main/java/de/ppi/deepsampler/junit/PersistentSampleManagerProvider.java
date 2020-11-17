package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.persistence.json.JsonSourceManager;

/**
 * A {@link PersistentSampleManagerProvider} may be used in combination with {@link LoadSamples} if the standard configuration of
 * a {@link JsonSourceManager} is insufficient. This could be the case e.g. if custom deserializers are needed.
 *
 * A concrete {@link PersistentSampleManagerProvider} must have a default constructor without parameters.
 */
public interface PersistentSampleManagerProvider {

    JsonSourceManager.Builder configurePersistentSampleManager();
}
