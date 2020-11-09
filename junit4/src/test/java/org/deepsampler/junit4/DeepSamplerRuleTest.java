package org.deepsampler.junit4;

import org.deepsampler.core.api.Sample;
import org.deepsampler.core.internal.ProxyFactory;
import org.deepsampler.core.model.SampleRepository;
import org.deepsampler.junit.PrepareSampler;
import org.deepsampler.junit.TestBean;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeepSamplerRuleTest {

    @Rule
    public DeepSamplerRule deepSamplerRule = new DeepSamplerRule();


    @PrepareSampler
    private TestBean testBeanSampler;

    @Test
    public void firstTestBeanHasBeenSampled() {
        assertNotNull(testBeanSampler);
        assertTrue(ProxyFactory.isProxyClass(testBeanSampler.getClass()));
    }

    @Test
    public void secondAddSamplesThatShouldBeClearedInNextTest() {
        Sample.of(testBeanSampler.getSomeInt()).is(1);
        assertFalse(SampleRepository.getInstance().isEmpty());
    }

    @Test
    public void thirdSamplesFromLastTestShouldBeCleared() {
        assertTrue(SampleRepository.getInstance().isEmpty());
    }
}