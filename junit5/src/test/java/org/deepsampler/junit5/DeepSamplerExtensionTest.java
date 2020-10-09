package org.deepsampler.junit5;

import org.deepsampler.core.api.Sample;
import org.deepsampler.core.internal.ProxyFactory;
import org.deepsampler.core.model.SampleRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DeepSamplerExtension.class)
public class DeepSamplerExtensionTest {

    @PrepareSampler
    private TestBean testBeanSampler;

    @Test
    @Order(1)
    public void testBeanHasBeenSampled() {
        assertNotNull(testBeanSampler);
        assertTrue(ProxyFactory.isProxyClass(testBeanSampler.getClass()));
    }

    @Test
    @Order(2)
    public void addSamplesThatShouldBeClearedInNextTest() {
        Sample.of(testBeanSampler.getSomeInt()).is(1);
        assertFalse(SampleRepository.getInstance().isEmpty());
    }

    @Test
    @Order(3)
    public void samplesFromLastTestShouldBeCleared() {
        assertTrue(SampleRepository.getInstance().isEmpty());
    }
}