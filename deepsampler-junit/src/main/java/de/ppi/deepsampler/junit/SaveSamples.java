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
 * The {@link SaveSamples}-annotation may be used at any test method as a convenient way to record samples from a running test. The recorded samples are saved as a JSON-file and
 * are usually loaded by @{@link LoadSamples}
 * <p>
 * The file is saved on the local file system (property {@link LoadSamples#file()}).
 * <p>
 * If no file-name is provided, DeepSampler creates a file name in the form {@code [simple class name]_[simple method name].json}. E.g. given a method
 * {@code org.project.MyTest#testIt()} the expected file name is
 * {@code ./org/project/MyTest_testIt.json}
 * <p>
 * This annotation must be used in combination with {@link UseSamplerFixture}.
 * <p>
 * The deserialization is done in two steps: First, Jackson is used to deserialize the JSON-File. If the file contains {@link de.ppi.deepsampler.persistence.model.PersistentBean}s
 * a second deserialization-step is run. The {@link de.ppi.deepsampler.persistence.model.PersistentBean} is an abstract model of a java Bean without any type information. The
 * second step reconstructs the type information from the sampler by analysing the sampled api and recreates the original object from the {@link de.ppi.deepsampler.persistence.model.PersistentBean}
 * <p>
 * It is possible to register customisations of the deserialization-process using the annotations @{@link UseJsonDeserializer} and @{@link UseBeanConverterExtension}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SaveSamples {

    /**
     * If this property is provided, the Sample-file will be saved to the local file system using the provided file name. If no file name is provided, the file name will be
     * generated in the form {@code [simple class name]_[simple method name].json}. E.g. given a method
     * {@code org.project.MyTest#testIt()} the expected file name is
     * {@code ./org/project/MyTest_testIt.json}
     *
     * @return the path to a Sampler-JSON-file.
     */
    String file() default "";
}
