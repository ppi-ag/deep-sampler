package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.core.model.SampledMethod;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JUnitPersistenceUtilsTest {

    @Test
    void unmatchedSerializerTypesAreDetected() throws NoSuchMethodException {
        // GIVEN
        Method errorProneMethod = Example.class.getMethod("wrongSerializerConfig");

        // THEN
        InvalidConfigException actualException = assertThrows(InvalidConfigException.class, () -> JUnitPersistenceUtils.loadSamples(errorProneMethod));
        assertEquals("de.ppi.deepsampler.junit.MyInstantSerializer must have a parameter type of type java.util.Date " +
                        "since the serializer is registered for the latter type. But it is java.time.Instant",
                actualException.getMessage());

    }

    @Test
    void unmatchedDeserializerTypesAreDetected() throws NoSuchMethodException {
        // GIVEN
        Method errorProneMethod = Example.class.getMethod("wrongDeserializerConfig");

        // THEN
        InvalidConfigException actualException = assertThrows(InvalidConfigException.class, () -> JUnitPersistenceUtils.loadSamples(errorProneMethod));
        assertEquals("de.ppi.deepsampler.junit.MyInstantDeserializer must have a parameter type of type java.util.Date " +
                        "since the deserializer is registered for the latter type. But it is java.time.Instant",
                actualException.getMessage());

    }

    @Test
    void innerClassAsSerializerCanBeUsed() throws NoSuchMethodException {
        // GIVEN
        Method methodWithInnerClassSerializer = Example.class.getMethod("loadInnerSerializerClass");

        SampledMethod sampledMethod = new SampledMethod(TestService.class, TestService.class.getMethod("getInstant"));
        SampleDefinition sampleDefinition = new SampleDefinition(sampledMethod);
        sampleDefinition.setMarkedForPersistence(true);
        SampleRepository.getInstance().add(sampleDefinition);

        // THEN
        assertDoesNotThrow(() -> JUnitPersistenceUtils.loadSamples(methodWithInnerClassSerializer));
    }

    public class Example {

        @LoadSamples
        @UseJsonSerializer(forType = Date.class, serializer = MyInstantSerializer.class)
        public void wrongSerializerConfig() {
            // nothing to do here because only the reaction to the annotations is tested.
        }

        @LoadSamples
        @UseJsonDeserializer(forType = Date.class, deserializer = MyInstantDeserializer.class)
        public void wrongDeserializerConfig() {
            // nothing to do here because only the reaction to the annotations is tested.
        }

        @LoadSamples
        @UseJsonDeserializer(forType = Instant.class, deserializer = MyInnerDateSerializer.class)
        public void loadInnerSerializerClass() {
            // nothing to do here because only the reaction to the annotations is tested.
        }

        public class MyInnerDateSerializer extends MyInstantDeserializer {
            // nothing to do here, because we simple use the inherited logic.
        }
    }
}
