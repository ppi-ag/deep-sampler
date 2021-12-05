/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit4;

import de.ppi.deepsampler.junit.GetSomeStringTestSampleFixture;
import de.ppi.deepsampler.junit.LoadSamples;
import de.ppi.deepsampler.junit.TestSampleFixture;
import de.ppi.deepsampler.junit.UseSamplerFixture;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasStubbedInt;
import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasStubbedString;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@UseSamplerFixture(TestSampleFixture.class)
public class SamplerFixtureTest {

    @Rule
    public DeepSamplerRule deepSamplerRule = new DeepSamplerRule();

    @Test
    @LoadSamples
    public void samplerFixtureAtClassLevelShouldBeUsed() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @LoadSamples
    @UseSamplerFixture(GetSomeStringTestSampleFixture.class)
    public void samplerFixtureAtMethodLevelShouldBeUsed() throws Throwable {
        assertTestBeanHasStubbedString();
    }
}