/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @{@link LoadSamples}-annotation may be used at any test method as a convenient way to load Samples from a
 * JSON-file. The JSON-file is usually created using @{@link SaveSamples}.
 * <p>
 * It is possible to load the file either from a local filesystem or from the classpath (property {@link LoadSamples#source()})
 * <p>
 * The file's name is composed of three parts: [rootPath][packagePath][fileName]. DeepSampler generates a default name using
 * the test class and test method. In most cases this will suffice, however you can change the path in detail:
 * <ul>
 *     <li>rootPath: The root path for relative packagePaths is by default './'. It can be changed, using the annotation
 *     {@link SampleRootPath}.
 *     <p>
 *     The rootPath is ignored, if the file is loaded from the classpath.</li>
 *     <li>packagePath: The path under the rootPath: If omitted, the package of the test class is used.</li>
 *     <li>fileName: the concrete name of the JSON-file: If omitted, the name of the test class and the test method is used.</li>
 * </ul>
 * <p>
 * This annotation must be used in combination with @{@link UseSamplerFixture}.
 * <p>
 * It is possible to register some extensions to customize the deserialization using the annotations @{@link UseJsonDeserializer}
 * and @{@link UseBeanConverterExtension}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoadSamples {

    /**
     * Defines the parent folder of {@link LoadSamples#fileName()}. Default is the package of the test class.
     * <p>
     * If {@link LoadSamples#source()} is set to {@link FileSource#FILE_SYSTEM}, packagePath is treated as a simple
     * path on the filesystem. If packagePath is a relative path, the root lies on './' by default. This root can be
     * changed using @{@link SampleRootPath}.
     *
     * @return the parent path of {@link LoadSamples#fileName()}. Default is the package of the test class.
     */
    String packagePath() default AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED;

    /**
     * The name of the json file without a path. Default is the name of the test class followed by the name of the test method.
     *
     * @return the name of the sample JSON file.
     */
    String fileName() default AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED;

    /**
     * Defines from where the file is loaded. Either the classpath or the vanilla filesystem can be used. Default is
     * {@link FileSource#CLASSPATH}.
     *
     * @return The source of the file.
     */
    FileSource source() default FileSource.CLASSPATH;
}
