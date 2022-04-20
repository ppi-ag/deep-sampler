/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit.json;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.junit.AnnotationConstants;
import de.ppi.deepsampler.junit.JUnitSamplerUtils;
import de.ppi.deepsampler.junit.SampleRootPath;
import de.ppi.deepsampler.junit.SourceManagerFactory;
import de.ppi.deepsampler.junit.UseCharset;
import de.ppi.deepsampler.persistence.json.JsonSourceManager;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@link JsonSourceManagerFactory} creates and configures a {@link JsonSourceManager}. It is used by the annotations {@link LoadSamples}
 * and {@link SaveSamples}, to configure the usage of JSON for recording and loading of samples.
 *
 * Tests, that want to use json-files to load and record samples, may use the following annotations to configure
 * the {@link JsonSourceManager}:
 * <ul>
 *     <li>{@link LoadSamples} loads a json-file before a test method is started</li>
 *     <li>{@link SaveSamples} records all calls to stubbed methods during a test and saves the recorded samples as a json-file after the
 *     test has finished</li>
 *     <li>{@link UseJsonDeserializer} registers a custom {@link JsonDeserializer} that is used by Jackson (the underlying json-api) to
 *     load objects, that cannot be loaded by default</li>
 *     <li>{@link UseJsonSerializer} registers a custom {@link JsonSerializer} that is used by Jackson (the underlying json-api) to
 *     save objects, that cannot be saved by default</li>
 *     <li>{@link SampleRootPath} defines the root path under which Json-files are saved.</li>
 *     <li>{@link UseCharset} defines the {@link Charset} that is used to write and read the Json-files</li>
 * </ul>
 */
public class JsonSourceManagerFactory implements SourceManagerFactory<JsonSourceManager> {

    public static final String DEFAULT_ROOT_PATH = "./";

    @Override
    public JsonSourceManager createSourceManagerToLoadSamples(final Method testMethod) {
        final JsonSourceManager.Builder persistentSampleManagerBuilder = JsonSourceManager.builder();
        final LoadSamples loadSamples = testMethod.getDeclaredAnnotation(LoadSamples.class);

        applyJsonSerializersFromTestCaseAndTestFixture(testMethod, persistentSampleManagerBuilder);
        applyCharsetFromTestCaseOrTestFixture(testMethod, persistentSampleManagerBuilder);

        final Optional<SampleRootPath> rootPath = JUnitSamplerUtils.loadAnnotationFromTestOrSampleFixture(testMethod, SampleRootPath.class);

        switch (loadSamples.source()) {
            case FILE_SYSTEM:
                final Path file = createPathForFilesystem(rootPath, loadSamples.value(), testMethod);
                return persistentSampleManagerBuilder.buildWithFile(file);
            case CLASSPATH:
            default:
                final String classPathResource = createPathForClasspath(loadSamples, testMethod);
                return persistentSampleManagerBuilder.buildWithClassPathResource(classPathResource, testMethod.getDeclaringClass());
        }
    }

    @Override
    public JsonSourceManager createSourceManagerToSaveSamples(final Method testMethod) {
        final JsonSourceManager.Builder persistentSampleManagerBuilder = JsonSourceManager.builder();
        final SaveSamples saveSamples = testMethod.getDeclaredAnnotation(SaveSamples.class);

        applyJsonSerializersFromTestCaseAndTestFixture(testMethod, persistentSampleManagerBuilder);
        applyCharsetFromTestCaseOrTestFixture(testMethod, persistentSampleManagerBuilder);

        final Optional<SampleRootPath> sampleRootPath = JUnitSamplerUtils.loadAnnotationFromTestOrSampleFixture(testMethod, SampleRootPath.class);
        final Path fileName = createPathForFilesystem(sampleRootPath, saveSamples.value(), testMethod);

        return persistentSampleManagerBuilder.buildWithFile(fileName);
    }


    private void applyJsonSerializersFromTestCaseAndTestFixture(final Method testMethod, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        // 1. Load serializers from SamplerFixture
        JUnitSamplerUtils.loadSamplerFixtureFromMethodOrDeclaringClass(testMethod).map(JUnitSamplerUtils::getDefineSamplersMethod)
                .ifPresent(samplerFixtureMethod -> {
                    applyAnnotatedJsonSerializers(samplerFixtureMethod, persistentSampleManagerBuilder);
                    applyAnnotatedJsonDeserializers(samplerFixtureMethod, persistentSampleManagerBuilder);
                });

        // 2. Load serializers from testMethod. Serializers from testMethod override the ones from the TestFixture.
        applyAnnotatedJsonSerializers(testMethod, persistentSampleManagerBuilder);
        applyAnnotatedJsonDeserializers(testMethod, persistentSampleManagerBuilder);
    }

    private void applyCharsetFromTestCaseOrTestFixture(final Method testMethod, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        JUnitSamplerUtils.loadAnnotationFromTestOrSampleFixture(testMethod, UseCharset.class)
                .map(UseCharset::value)
                .map(Charset::forName)
                .ifPresent(persistentSampleManagerBuilder::withCharset);
    }

    /**
     * Creates a {@link Path} based on the three path elements. If any of them is missing, a default value will be used. The
     * path is concatenated like this: [rootPath][packagePath][fileName].
     *
     * @param sampleRootPath the annotation {@link SampleRootPath} that is used to configure the root path. If it is not
     *                       supplied, the default ./ will be used.
     * @param file           A file name, that is resolved under rootPath. If it is not supplied, the package of the class,
     *                       that declares testMethod, the test class name and the test method name is used.
     *                       file must not be null. The String
     *                       {@link AnnotationConstants#DEFAULT_VALUE_MUST_BE_CALCULATED} will be treated as not-supplied.
     *                       This is because the empty default value of {@link LoadSamples#value()} cannot be null.
     * @param testMethod     The test method that is running the current test. It is used to provide default values.
     * @return A path that is formatted to be used on the file system (in contrast to a path, that is formatted to be used
     * on the classpath, see {@link #createPathForClasspath(LoadSamples, Method)}.
     */
    Path createPathForFilesystem(final Optional<SampleRootPath> sampleRootPath, final String file, final Method testMethod) {
        final Path path = sampleRootPath.map(a -> Paths.get(a.value())).orElse(Paths.get(DEFAULT_ROOT_PATH));

        if (file.equals(AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED)) {
            return path.resolve(testMethod.getDeclaringClass().getPackage().getName().replace(".", "/"))
                    .resolve(getDefaultJsonFileName(testMethod));
        } else {
            return path.resolve(file.replaceFirst("^[/\\\\]", ""));
        }
    }

    /**
     * <p>
     * Creates a Path that is used to load a sample file from the classpath. The path is based on two elements:
     * <p>
     * [packagePath][fileName]
     * <p>
     * If any of these two are missing, a default value will be used.
     *
     * @param loadSamples provides the packagePath and the fileName. If packagePath is not supplied, the package of
     *                    testMethod is used. If fileName is not supplied, the name of testMethod and it's declaring
     *                    class is used.
     * @param testMethod  the method that runs the current test. It is used to provide default values for path elements.
     * @return a path that can be used to load a sample file from the classpath.
     */
    String createPathForClasspath(final LoadSamples loadSamples, final Method testMethod) {
        if (loadSamples.value().equals(AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED)) {
            return "/" + testMethod.getDeclaringClass().getPackage().getName().replace(".", "/")
                    + "/" + getDefaultJsonFileName(testMethod);
        } else {
            return loadSamples.value();
        }
    }

    public String getDefaultJsonFileName(final Method testMethod) {
        return testMethod.getDeclaringClass().getSimpleName() + "_" + testMethod.getName() + ".json";
    }

    /**
     * Activates {@link UseJsonSerializers} from testMethod and it's declaring class. If both are annotated, both are activated.
     * If testMethod declares {@link JsonSerializer}s for the same types as it's declaring class, the ones from testMethod override the ones from the class.
     *
     * @param testMethod the test-method for which the {@link JsonSerializer}s shall be activated. The method, or it's declaring class must be annotated with
     *                   {@link UseJsonSerializer}
     * @param persistentSampleManagerBuilder the {@link JsonSerializer}s from testMethod will be applied to persistentSampleManagerBuilder.
     */
    private void applyAnnotatedJsonSerializers(final Method testMethod, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final UseJsonSerializer[] serializersOnMethod = testMethod.getAnnotationsByType(UseJsonSerializer.class);
        final UseJsonSerializer[] serializersOnClass = testMethod.getDeclaringClass().getAnnotationsByType(UseJsonSerializer.class);

        Stream.of(serializersOnClass, serializersOnMethod)
                .flatMap(Stream::of)
                .forEach(serializerAnnotation -> addSerializer(serializerAnnotation, persistentSampleManagerBuilder));
    }

    /**
     * Activates {@link UseJsonDeserializer}s from testMethod and it's declaring class. If both are annotated, both are activated.
     * If testMethod declares {@link JsonDeserializer}s for the same types as it's declaring class, the ones from testMethod override the ones from the class.
     *
     * @param testMethod the test-method for which the {@link JsonDeserializer}s shall be activated. The method, or it's declaring class must be annotated with
     *      *                   {@link UseJsonDeserializer}
     * @param persistentSampleManagerBuilder the {@link JsonDeserializer}s from testMethod will be applied to persistentSampleManagerBuilder.
     */
    private void applyAnnotatedJsonDeserializers(final Method testMethod, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final UseJsonDeserializer[] deserializersOnMethod = testMethod.getAnnotationsByType(UseJsonDeserializer.class);
        final UseJsonDeserializer[] deserializersOnClass = testMethod.getDeclaringClass().getAnnotationsByType(UseJsonDeserializer.class);

        Stream.of(deserializersOnClass, deserializersOnMethod)
                .flatMap(Stream::of)
                .forEach(deserializerAnnotation -> addDeserializer(deserializerAnnotation, persistentSampleManagerBuilder));
    }

    private <T> void addSerializer(final UseJsonSerializer useJsonSerializer, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final Class<? extends JsonSerializer<T>> serializerClass = (Class<? extends JsonSerializer<T>>) useJsonSerializer.serializer();
        final Class<T> typeToSerialize = (Class<T>) useJsonSerializer.forType();

        validateTypesOfSerializerAndSerializable(serializerClass, typeToSerialize);

        final JsonSerializer<T> jsonSerializer = JUnitSamplerUtils.instantiate(serializerClass);

        persistentSampleManagerBuilder.addSerializer(typeToSerialize, jsonSerializer);
    }

    private <T> void addDeserializer(final UseJsonDeserializer useJsonDeserializer, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final Class<? extends JsonDeserializer<T>> deserializerClass = (Class<? extends JsonDeserializer<T>>) useJsonDeserializer.deserializer();
        final Class<T> typeToDeserialize = (Class<T>) useJsonDeserializer.forType();

        validateTypesOfDeserializerAndDeserializable(deserializerClass, typeToDeserialize);

        final JsonDeserializer<T> jsonDeserializer = JUnitSamplerUtils.instantiate(deserializerClass);

        persistentSampleManagerBuilder.addDeserializer(typeToDeserialize, jsonDeserializer);
    }

    /**
     * Checks if the generic type of the serializer is the same as the type of the class that should be serialized by the serializer. Usually this would be
     * ensured by generics at compile time, but annotations don't allow the usage of generics other than wildcards.
     *
     * @param serializerClass the type of the {@link JsonSerializer}
     * @param typeToSerialize the type of the classes that should be serialized by serializerClass
     */
    private void validateTypesOfSerializerAndSerializable(final Class<? extends JsonSerializer<?>> serializerClass, final Class<?> typeToSerialize) {
        final Type[] typeArguments = getParameterizedParentType(serializerClass).getActualTypeArguments();

        if (!typeArguments[0].equals(typeToSerialize)) {
            throw new InvalidConfigException("%s must have a parameter of type %s since the serializer is registered for the latter type. But it is %s",
                    serializerClass.getName(),
                    typeToSerialize.getName(),
                    typeArguments[0].getTypeName());
        }
    }

    /**
     * Checks if the generic type of the serializer is the same as the type of the class that should be serialized by the serializer. Usually this would be
     * ensured by generics at compile time, but annotations don't allow the usage of generics other than wildcards.
     *
     * @param deserializerClass the type of the {@link JsonDeserializer}
     * @param typeToDeserialize the type of the classes that should be deserialized by deserializerClass
     */
    private void validateTypesOfDeserializerAndDeserializable(final Class<? extends JsonDeserializer<?>> deserializerClass, final Class<?> typeToDeserialize) {
        final Type[] typeArguments = getParameterizedParentType(deserializerClass).getActualTypeArguments();

        if (!typeArguments[0].equals(typeToDeserialize)) {
            throw new InvalidConfigException("%s must have a parameter of type %s since the deserializer is registered for the latter type. But it is %s",
                    deserializerClass.getName(),
                    typeToDeserialize.getName(),
                    typeArguments[0].getTypeName());
        }
    }

    /**
     * If clazz is a subclass that does not declare a generic type parameter on its own and relies on the type parameter of a
     * parent class, we have to search the type hierarchy for the first parameterized type.
     *
     * @param clazz the class for which we want to find the first parameterized superclass.
     * @return the first parameterized superclass
     */
    private ParameterizedType getParameterizedParentType(final Class<?> clazz) {
        if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
            return (ParameterizedType) clazz.getGenericSuperclass();
        }

        return getParameterizedParentType((Class<?>) clazz.getGenericSuperclass());
    }
}
