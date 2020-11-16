package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.model.Answer;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.core.model.SampledMethod;
import de.ppi.deepsampler.core.model.StubMethodInvocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A simple utility that contains some reusable test methods that are used in both junit-plugins.
 */
public class JUnitTestUtility {

    private JUnitTestUtility() {
        // This utility class is not intended to be instantiated.
    }

    /**
     * Proves that {@link TestBean} has a Sampler in {@link SampleRepository} and that the Sample for the {@link TestBean#getSomeInt()} method
     * is 42. this value should be provided by an arbitrary Sampler, since the default implementarion would return 0.
     *
     * @throws Exception the generic call to an {@link Answer#call(StubMethodInvocation)} may yield an Exception of any kind if the concrete
     * implementation decides that this is necessary.
     */
    public static void assertTestBeanHasBeenStubbed() throws Exception {
        final SampleRepository sampleRepository = SampleRepository.getInstance();

        assertFalse(sampleRepository.isEmpty());

        final SampledMethod expectedSampledMethod = new SampledMethod(TestBean.class, TestBean.class.getMethod("getSomeInt"));
        final SampleDefinition getSomeInt = sampleRepository.findAllForMethod(expectedSampledMethod).get(0);

        assertEquals(42, getSomeInt.getAnswer().call(null));
    }

    /**
     * Proves that path does not exist. However, if it exists, it is deleted.
     * @param path the path of the file that must must not exist.
     * @throws IOException In case the file cannot be deleted.
     */
    public static void assertThatFileDoesNotExistOrOtherwiseDeleteIt(final Path path) throws IOException {
        if (Files.exists(path)) {
            Files.delete(path);
        }

        assertFalse(Files.exists(path));
    }
}
