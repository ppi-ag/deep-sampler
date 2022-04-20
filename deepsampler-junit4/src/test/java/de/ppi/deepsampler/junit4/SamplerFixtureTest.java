/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit4;

import de.ppi.deepsampler.junit.FileSource;
import de.ppi.deepsampler.junit.GetSomeStringTestSampleFixture;
import de.ppi.deepsampler.junit.TestSampleFixture;
import de.ppi.deepsampler.junit.UseSamplerFixture;
import de.ppi.deepsampler.junit.json.LoadSamples;
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
    @LoadSamples(source = FileSource.CLASSPATH)
    public void samplerFixtureAtClassLevelShouldBeUsed() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @LoadSamples(source = FileSource.CLASSPATH)
    @UseSamplerFixture(GetSomeStringTestSampleFixture.class)
    public void samplerFixtureAtMethodLevelShouldBeUsed() throws Throwable {
        assertTestBeanHasStubbedString();
    }
}