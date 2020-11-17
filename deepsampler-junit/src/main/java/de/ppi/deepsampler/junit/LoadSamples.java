package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.persistence.json.JsonSourceManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link LoadSamples}-annotation may be used at any test method as a convenient way to load Samples from a JSON-file.
 *
 * It is possible to load the file either from a local file system (property {@link LoadSamples#file()}) or from the classpath (property {@link LoadSamples#classPath()})
 *
 * If no file-name is provided, DeepSampler loads the file from the classpath, expecting that the file is located in the same package as the test class
 * and is named in the form {@code [simple class name]_[simple method name].json}. E. g. given a method {@code org.project.MyTest#testIt()} the expected file name is
 * {@code /org/project/MyTest_testIt.json}
 *
 * It is possible to define a custom configured {@link JsonSourceManager} in case some extensions for the JSON-persistence are
 * needed. The {@link JsonSourceManager} can be created using a {@link PersistentSampleManagerProvider}.
 *
 * This annotation must be used in combination with {@link UseSamplerFixture}, since Samplers must be defined prior to loading the Samples from file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoadSamples {

    Class<? extends PersistentSampleManagerProvider> persistenceManagerProvider() default DefaultPersistentSampleManagerProvider.class;

    /**
     * If this property is provided, the Sampler-file will be loaded from the local file system using the provided file name.
     * @return the path to a Sampler-JSON-file.
     */
    String file() default "";

    /**
     * If this property is provided, the Sampler-file will be loaded from the classpath using the provided resource-name.
     * @return the path to a Sampler-JSON-file. The path is resolved relative to the test class.
     */
    String classPath() default "";
}
