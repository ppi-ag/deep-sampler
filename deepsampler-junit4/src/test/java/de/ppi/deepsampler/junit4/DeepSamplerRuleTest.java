/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit4;

import de.ppi.deepsampler.core.api.Sample;
import de.ppi.deepsampler.core.internal.ProxyFactory;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.junit.PrepareSampler;
import de.ppi.deepsampler.junit.TestBean;
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