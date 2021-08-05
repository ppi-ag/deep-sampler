/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit4;

import de.ppi.deepsampler.junit.*;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasBeenHasStubbedInt;
import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasBeenHasStubbedString;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@UseSamplerFixture(TestSampleFixture.class)
public class SamplerFixtureTest {

    @Rule
    public DeepSamplerRule deepSamplerRule = new DeepSamplerRule();

    @Test
    @LoadSamples
    public void samplerFixtureAtClassLevelShouldBeUsed() throws Throwable {
        assertTestBeanHasBeenHasStubbedInt();
    }

    @Test
    @LoadSamples
    @UseSamplerFixture(GetSomeStringTestSampleFixture.class)
    public void samplerFixtureAtMethodLevelShouldBeUsed() throws Throwable {
        assertTestBeanHasBeenHasStubbedString();
    }
}