/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;


import de.ppi.deepsampler.core.api.PersistentSample;
import de.ppi.deepsampler.core.api.Sample;
import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.core.error.NoMatchingParametersFoundException;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.core.model.SampledMethod;
import de.ppi.deepsampler.persistence.error.NoMatchingSamplerFoundException;
import de.ppi.deepsampler.persistence.model.PersistentActualSample;
import de.ppi.deepsampler.persistence.model.PersistentMethodCall;
import de.ppi.deepsampler.persistence.model.PersistentModel;
import de.ppi.deepsampler.persistence.model.PersistentParameter;
import de.ppi.deepsampler.persistence.model.PersistentSampleMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static de.ppi.deepsampler.core.api.Matchers.any;
import static de.ppi.deepsampler.core.api.Matchers.equalTo;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecorded;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.combo;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.equalsMatcher;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.sameMatcher;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersistentSampleManagerTest {

    @Test
    void testLoadWithComboMatcher() throws NoSuchMethodException {
        // GIVEN
        final SourceManager mockedSourceManager = mock(SourceManager.class);
        final PersistentModel mockedPersistentModel = mock(PersistentModel.class);

        when(mockedPersistentModel.getId()).thenReturn("ID");

        final Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        final PersistentSampleMethod mockedMethod = mock(PersistentSampleMethod.class);
        final PersistentActualSample mockedSample = mock(PersistentActualSample.class);

        final List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        sampleMap.put(mockedMethod, mockedSample);

        when(mockedMethod.getSampleMethodId()).thenReturn("SampleId");
        when(mockedSample.getAllCalls()).thenReturn(persistentMethodCallList);
        when(mockedPersistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);
        when(mockedSourceManager.load()).thenReturn(mockedPersistentModel);

        final TestBean givenBean = new TestBean();
        addMethodCall(persistentMethodCallList, Arrays.asList(givenBean, 1), true);

        final PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);
        PersistentSample.of(Sampler.prepare(TestService.class).call(combo(equalTo(new TestBean()), sameMatcher()),
                equalTo(1))).hasId("SampleId");

        // WHEN
        persistentSampleManager.load();

        // THEN
        assertFalse(SampleRepository.getInstance().isEmpty());
        final SampleDefinition expectedCall = new SampleDefinition(new SampledMethod(TestService.class, TestService.class.getDeclaredMethod("call", TestBean.class, Integer.class)));
        expectedCall.setSampleId("SampleId");
        expectedCall.setParameterValues(Arrays.asList(new TestBean(), 1));
        assertEquals(expectedCall,
                SampleRepository.getInstance().getSamples().get(0));

        assertFalse(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(0, TestBean.class).matches(new TestBean()));
        assertTrue(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(0, TestBean.class).matches(givenBean));
    }

    @Test
    void testLoadWithCustomMatcher() throws NoSuchMethodException {
        // GIVEN
        final SourceManager mockedSourceManager = mock(SourceManager.class);
        final PersistentModel mockedPersistentModel = mock(PersistentModel.class);

        when(mockedPersistentModel.getId()).thenReturn("ID");

        final Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        final PersistentSampleMethod mockedMethod = mock(PersistentSampleMethod.class);
        final PersistentActualSample mockedSample = mock(PersistentActualSample.class);

        final List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        sampleMap.put(mockedMethod, mockedSample);

        when(mockedMethod.getSampleMethodId()).thenReturn("SampleId");
        when(mockedSample.getAllCalls()).thenReturn(persistentMethodCallList);
        when(mockedPersistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);
        when(mockedSourceManager.load()).thenReturn(mockedPersistentModel);

        final TestBean givenBean = new TestBean();
        givenBean.someString = "a";
        addMethodCall(persistentMethodCallList, Arrays.asList(givenBean, 1), true);

        final PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.call(anyRecorded((a, b) -> a.someString.equals(b.someString)), anyRecorded(Objects::equals))).hasId("SampleId");

        // WHEN
        persistentSampleManager.load();

        // THEN
        final SampleDefinition actualCall = SampleRepository.getInstance().getSamples().get(0);

        assertFalse(SampleRepository.getInstance().isEmpty());

        assertEquals("SampleId", actualCall.getSampleId());
        assertEquals(TestService.class.getDeclaredMethod("call", TestBean.class, Integer.class), actualCall.getSampledMethod().getMethod());
        assertEquals(TestService.class, actualCall.getSampledMethod().getTarget());
        assertEquals(Arrays.asList(givenBean, 1), actualCall.getParameterValues());

        assertFalse(actualCall.getParameterMatcherAs(0, TestBean.class).matches(new TestBean("wrong")));
        assertTrue(actualCall.getParameterMatcherAs(0, TestBean.class).matches(givenBean));
    }

    @Test
    void detectsCompletelyMissingSample() {
        // GIVEN
        final SourceManager mockedSourceManager = mock(SourceManager.class);
        final PersistentModel persistentModel = mock(PersistentModel.class);
        when(persistentModel.getId()).thenReturn("ID");
        when(mockedSourceManager.load()).thenReturn(persistentModel);

        final Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        final PersistentSampleMethod method = mock(PersistentSampleMethod.class);
        final PersistentActualSample sample = mock(PersistentActualSample.class);
        final PersistentSampleMethod unexpectedMethod = mock(PersistentSampleMethod.class);
        final PersistentActualSample unexpectedSample = mock(PersistentActualSample.class);
        final List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        sampleMap.put(method, sample);
        sampleMap.put(unexpectedMethod, unexpectedSample);
        when(persistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);

        when(method.getSampleMethodId()).thenReturn("SampleId");
        when(sample.getAllCalls()).thenReturn(persistentMethodCallList);
        when(unexpectedMethod.getSampleMethodId()).thenReturn("UnexpectedSampleId");
        when(unexpectedSample.getAllCalls()).thenReturn(persistentMethodCallList);

        // WHEN
        final TestBean givenBean = new TestBean();
        addMethodCall(persistentMethodCallList, Arrays.asList(givenBean, 1), true);

        final PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);
        PersistentSample.of(Sampler.prepare(TestService.class).call(any(TestBean.class), any(Integer.class))).hasId("SampleId");

        // THEN
        final NoMatchingSamplerFoundException expectedException = assertThrows(NoMatchingSamplerFoundException.class, persistentSampleManager::load);
        // There is no sampleid that has any similarity, so the Exception doesn't show a guess which sampleid could have been the correct one:
        assertEquals("The following persistent Samples don't have a corresponding Sampler. Please define a Sampler using PersistentSampler.of(...):\n" +
                "\tUnexpectedSampleId", expectedException.getMessage());
    }

    @Test
    void detectsMissingSampleWithSlightlyWrongSampleId() {
        // GIVEN
        final SourceManager mockedSourceManager = mock(SourceManager.class);
        final PersistentModel persistentModel = mock(PersistentModel.class);
        when(persistentModel.getId()).thenReturn("ID");
        when(mockedSourceManager.load()).thenReturn(persistentModel);

        final Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        final PersistentSampleMethod method = mock(PersistentSampleMethod.class);
        final PersistentActualSample sample = mock(PersistentActualSample.class);
        final PersistentSampleMethod unexpectedMethod = mock(PersistentSampleMethod.class);
        final PersistentActualSample unexpectedSample = mock(PersistentActualSample.class);
        final List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        sampleMap.put(method, sample);
        sampleMap.put(unexpectedMethod, unexpectedSample);
        when(persistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);

        when(method.getSampleMethodId()).thenReturn("SampleId");
        when(sample.getAllCalls()).thenReturn(persistentMethodCallList);
        when(unexpectedMethod.getSampleMethodId()).thenReturn("ASampleId");
        when(unexpectedSample.getAllCalls()).thenReturn(persistentMethodCallList);

        // WHEN
        final TestBean givenBean = new TestBean();
        addMethodCall(persistentMethodCallList, Arrays.asList(givenBean, 1), true);

        final PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);
        PersistentSample.of(Sampler.prepare(TestService.class).call(any(TestBean.class), any(Integer.class))).hasId("SampleId");

        // THEN
        final NoMatchingSamplerFoundException expectedException = assertThrows(NoMatchingSamplerFoundException.class, persistentSampleManager::load);
        assertEquals("The following persistent Samples don't have a corresponding Sampler. Please define a Sampler using PersistentSampler.of(...):\n" +
                "\tASampleId\n" +
                "\t\t did you mean SampleId?", expectedException.getMessage());
    }

    @Test
    void detectsMissingSampleThatIsNotMarkedForPersistence() {
        // GIVEN
        final SourceManager mockedSourceManager = mock(SourceManager.class);
        final PersistentModel persistentModel = mock(PersistentModel.class);
        when(persistentModel.getId()).thenReturn("ID");
        when(mockedSourceManager.load()).thenReturn(persistentModel);

        final Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        final PersistentSampleMethod method = mock(PersistentSampleMethod.class);
        final PersistentActualSample sample = mock(PersistentActualSample.class);
        final PersistentSampleMethod unexpectedMethod = mock(PersistentSampleMethod.class);
        final PersistentActualSample unexpectedSample = mock(PersistentActualSample.class);
        final List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        sampleMap.put(method, sample);
        sampleMap.put(unexpectedMethod, unexpectedSample);
        when(persistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);

        when(method.getSampleMethodId()).thenReturn("SampleId");
        when(sample.getAllCalls()).thenReturn(persistentMethodCallList);
        when(unexpectedMethod.getSampleMethodId()).thenReturn("boolean de.ppi.deepsampler.persistence.api.PersistentSampleManagerTest$TestService.call(de.ppi.deepsampler.persistence.api.PersistentSampleManagerTest$TestBean,java.lang.Integer)");
        when(unexpectedSample.getAllCalls()).thenReturn(persistentMethodCallList);

        // WHEN
        final TestBean givenBean = new TestBean();
        addMethodCall(persistentMethodCallList, Arrays.asList(givenBean, 1), true);

        final PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);
        Sample.of(Sampler.prepare(TestService.class).call(any(TestBean.class), any(Integer.class)));

        // THEN
        final NoMatchingSamplerFoundException expectedException = assertThrows(NoMatchingSamplerFoundException.class, persistentSampleManager::load);
        assertEquals("The following persistent Samples don't have a corresponding Sampler. Please define a Sampler using PersistentSampler.of(...):\n" +
                "\tboolean de.ppi.deepsampler.persistence.api.PersistentSampleManagerTest$TestService.call(de.ppi.deepsampler.persistence.api.PersistentSampleManagerTest$TestBean,java.lang.Integer)\n" +
                "\t\tboolean de.ppi.deepsampler.persistence.api.PersistentSampleManagerTest$TestService.call(de.ppi.deepsampler.persistence.api.PersistentSampleManagerTest$TestBean,java.lang.Integer) seems to be quite similar, but it was not marked for persistence. Use PersistentSampler.of() instead of Sampler.of(), if the Sample should be provided from persistence.\n" +
                "\tSampleId", expectedException.getMessage());
    }

    @Test
    void detectsMissingWrongParameters() {
        // GIVEN
        final SourceManager mockedSourceManager = mock(SourceManager.class);
        final PersistentModel persistentModel = mock(PersistentModel.class);
        when(persistentModel.getId()).thenReturn("ID");
        when(mockedSourceManager.load()).thenReturn(persistentModel);

        final Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        final PersistentSampleMethod method = mock(PersistentSampleMethod.class);
        final PersistentActualSample sample = mock(PersistentActualSample.class);
        sampleMap.put(method, sample);

        final List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        when(persistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);

        when(method.getSampleMethodId()).thenReturn("SampleId");
        when(sample.getAllCalls()).thenReturn(persistentMethodCallList);

        // WHEN
        final TestBean givenBean = new TestBean();
        addMethodCall(persistentMethodCallList, Arrays.asList(null, 2), true);

        final PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);
        final TestService sampler = Sampler.prepare(TestService.class);
        PersistentSample.of(sampler.call(givenBean, 4)).hasId("SampleId");

        // THEN
        assertThrows(NoMatchingParametersFoundException.class, persistentSampleManager::load);
    }

    @Test
    void testLoadWithMultiComboMatcher() throws NoSuchMethodException {
        // GIVEN
        final SourceManager mockedSourceManager = mock(SourceManager.class);
        final PersistentModel persistentModel = mock(PersistentModel.class);
        when(persistentModel.getId()).thenReturn("ID");
        final Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        final PersistentSampleMethod method = mock(PersistentSampleMethod.class);
        final PersistentActualSample sample = mock(PersistentActualSample.class);
        final List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        sampleMap.put(method, sample);
        when(method.getSampleMethodId()).thenReturn("SampleId");
        when(sample.getAllCalls()).thenReturn(persistentMethodCallList);
        when(persistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);
        when(mockedSourceManager.load()).thenReturn(persistentModel);

        addMethodCall(persistentMethodCallList, Arrays.asList(new TestBean(), 1), false);

        final PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);
        PersistentSample.of(Sampler.prepare(TestService.class).call(combo(equalTo(new TestBean()), equalsMatcher()),
                combo(equalTo(1), sameMatcher()))).hasId("SampleId");

        // WHEN
        persistentSampleManager.load();

        // THEN
        assertFalse(SampleRepository.getInstance().isEmpty());
        final SampleDefinition expectedCall = new SampleDefinition(new SampledMethod(TestService.class, TestService.class.getDeclaredMethod("call", TestBean.class, Integer.class)));
        expectedCall.setSampleId("SampleId");
        expectedCall.setParameterValues(Arrays.asList(new TestBean(), 1));
        assertEquals(expectedCall,
                SampleRepository.getInstance().getSamples().get(0));
        assertTrue(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(0, TestBean.class).matches(new TestBean()));
        final TestBean testBean = new TestBean();
        testBean.someString = "NOT NULL";
        assertFalse(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(0, TestBean.class).matches(testBean));

        assertTrue(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(1, Integer.class).matches(1));
        assertFalse(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(1, Integer.class).matches(2));
    }

    @AfterEach
    void clean() {
        SampleRepository.getInstance().clear();
    }

    private void addMethodCall(final List<PersistentMethodCall> persistentMethodCallList, final List<Object> params, final Object returnValue) {
        final PersistentParameter persistentParameter = mock(PersistentParameter.class);
        final PersistentMethodCall persistentMethodCall = mock(PersistentMethodCall.class);
        persistentMethodCallList.add(persistentMethodCall);
        when(persistentMethodCall.getPersistentParameter()).thenReturn(persistentParameter);
        when(persistentMethodCall.getPersistentReturnValue()).thenReturn(returnValue);
        when(persistentParameter.getParameter()).thenReturn(params);
    }

    private static class TestService {
        boolean call(final TestBean testBean, final Integer i) {
            return false;
        }
    }

    private static class TestBean {
        private ComplicatedBean complicatedBean;
        private String someString;
        private Double someDouble;

        public TestBean() {
        }

        public TestBean(final String someString) {
            this.someString = someString;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final TestBean testBean = (TestBean) o;
            return Objects.equals(complicatedBean, testBean.complicatedBean) && Objects.equals(someString, testBean.someString) && Objects.equals(someDouble, testBean.someDouble);
        }

        @Override
        public int hashCode() {
            return Objects.hash(complicatedBean, someString, someDouble);
        }
    }

    private static class ComplicatedBean {
        ComplicatedBean complicatedBean;
        int index;
        Integer index2;
    }
}
