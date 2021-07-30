package de.ppi.deepsampler.persistence.api;


import de.ppi.deepsampler.core.api.Sample;
import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.core.model.SampledMethod;
import de.ppi.deepsampler.persistence.error.ParametersAreNotMatchedException;
import de.ppi.deepsampler.persistence.error.NoMatchingSamplerFoundException;
import de.ppi.deepsampler.persistence.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static de.ppi.deepsampler.core.api.Matchers.*;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersistentSampleManagerTest {

    @Test
    void testLoadWithComboMatcher() throws NoSuchMethodException {
        // GIVEN
        SourceManager mockedSourceManager = mock(SourceManager.class);
        PersistentModel mockedPersistentModel = mock(PersistentModel.class);

        when(mockedPersistentModel.getId()).thenReturn("ID");

        Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        PersistentSampleMethod mockedMethod = mock(PersistentSampleMethod.class);
        PersistentActualSample mockedSample = mock(PersistentActualSample.class);

        List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        sampleMap.put(mockedMethod, mockedSample);

        when(mockedMethod.getSampleMethodId()).thenReturn("SampleId");
        when(mockedSample.getAllCalls()).thenReturn(persistentMethodCallList);
        when(mockedPersistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);
        when(mockedSourceManager.load()).thenReturn(mockedPersistentModel);

        TestBean givenBean = new TestBean();
        addMethodCall(persistentMethodCallList, Arrays.asList(givenBean, 1), true);

        PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);
        Sample.of(Sampler.prepare(TestService.class).call(combo(equalTo(new TestBean()), sameMatcher()),
                equalTo(1))).hasId("SampleId");

        // WHEN
        persistentSampleManager.load();

        // THEN
        assertFalse(SampleRepository.getInstance().isEmpty());
        SampleDefinition expectedCall = new SampleDefinition(new SampledMethod(TestService.class, TestService.class.getDeclaredMethod("call", TestBean.class, Integer.class)));
        expectedCall.setSampleId("SampleId");
        expectedCall.setParameterValues(Arrays.asList(new TestBean(), 1));
        assertEquals(expectedCall,
                SampleRepository.getInstance().getSamples().get(0));

        assertFalse(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(0, TestBean.class).matches(new TestBean()));
        assertTrue(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(0, TestBean.class).matches(givenBean));
    }

    @Test
    void detectsCompletelyMissingSample() {
        // GIVEN
        SourceManager mockedSourceManager = mock(SourceManager.class);
        PersistentModel persistentModel = mock(PersistentModel.class);
        when(persistentModel.getId()).thenReturn("ID");
        when(mockedSourceManager.load()).thenReturn(persistentModel);

        Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        PersistentSampleMethod method = mock(PersistentSampleMethod.class);
        PersistentActualSample sample = mock(PersistentActualSample.class);
        PersistentSampleMethod unexpectedMethod = mock(PersistentSampleMethod.class);
        PersistentActualSample unexpectedSample = mock(PersistentActualSample.class);
        List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        sampleMap.put(method, sample);
        sampleMap.put(unexpectedMethod, unexpectedSample);
        when(persistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);

        when(method.getSampleMethodId()).thenReturn("SampleId");
        when(sample.getAllCalls()).thenReturn(persistentMethodCallList);
        when(unexpectedMethod.getSampleMethodId()).thenReturn("UnexpectedSampleId");
        when(unexpectedSample.getAllCalls()).thenReturn(persistentMethodCallList);


        TestBean givenBean = new TestBean();
        addMethodCall(persistentMethodCallList, Arrays.asList(givenBean, 1), true);

        PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);
        Sample.of(Sampler.prepare(TestService.class).call(any(TestBean.class), any(Integer.class))).hasId("SampleId");

        // WHEN
        assertThrows(NoMatchingSamplerFoundException.class, persistentSampleManager::load);
    }

    @Test
    void detectsMissingSampleMatchers() {
        // GIVEN
        SourceManager mockedSourceManager = mock(SourceManager.class);
        PersistentModel persistentModel = mock(PersistentModel.class);
        when(persistentModel.getId()).thenReturn("ID");
        when(mockedSourceManager.load()).thenReturn(persistentModel);

        Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        PersistentSampleMethod method = mock(PersistentSampleMethod.class);
        PersistentActualSample sample = mock(PersistentActualSample.class);
        sampleMap.put(method, sample);

        List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        when(persistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);

        when(method.getSampleMethodId()).thenReturn("SampleId");
        when(sample.getAllCalls()).thenReturn(persistentMethodCallList);


        TestBean givenBean = new TestBean();
        addMethodCall(persistentMethodCallList, Arrays.asList(null, 2), true);

        PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);
        TestService sampler = Sampler.prepare(TestService.class);
        Sample.of(sampler.call(givenBean, 4)).hasId("SampleId");

        // WHEN
        assertThrows(ParametersAreNotMatchedException.class, persistentSampleManager::load);
    }

    @Test
    void testLoadWithMultiComboMatcher() throws NoSuchMethodException {
        // GIVEN
        SourceManager mockedSourceManager = mock(SourceManager.class);
        PersistentModel persistentModel = mock(PersistentModel.class);
        when(persistentModel.getId()).thenReturn("ID");
        Map<PersistentSampleMethod, PersistentActualSample> sampleMap = new HashMap<>();
        PersistentSampleMethod method = mock(PersistentSampleMethod.class);
        PersistentActualSample sample = mock(PersistentActualSample.class);
        List<PersistentMethodCall> persistentMethodCallList = new ArrayList<>();
        sampleMap.put(method, sample);
        when(method.getSampleMethodId()).thenReturn("SampleId");
        when(sample.getAllCalls()).thenReturn(persistentMethodCallList);
        when(persistentModel.getSampleMethodToSampleMap()).thenReturn(sampleMap);
        when(mockedSourceManager.load()).thenReturn(persistentModel);

        addMethodCall(persistentMethodCallList, Arrays.asList(new TestBean(), 1), false);

        PersistentSampleManager persistentSampleManager = new PersistentSampleManager(mockedSourceManager);
        Sample.of(Sampler.prepare(TestService.class).call(combo(equalTo(new TestBean()), equalsMatcher()),
                combo(equalTo(1), sameMatcher()))).hasId("SampleId");

        // WHEN
        persistentSampleManager.load();

        // THEN
        assertFalse(SampleRepository.getInstance().isEmpty());
        SampleDefinition expectedCall = new SampleDefinition(new SampledMethod(TestService.class, TestService.class.getDeclaredMethod("call", TestBean.class, Integer.class)));
        expectedCall.setSampleId("SampleId");
        expectedCall.setParameterValues(Arrays.asList(new TestBean(), 1));
        assertEquals(expectedCall,
                SampleRepository.getInstance().getSamples().get(0));
        assertTrue(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(0, TestBean.class).matches(new TestBean()));
        TestBean testBean = new TestBean();
        testBean.someString = "NOT NULL";
        assertFalse(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(0, TestBean.class).matches(testBean));

        assertTrue(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(1, Integer.class).matches(1));
        assertFalse(SampleRepository.getInstance().getSamples().get(0).getParameterMatcherAs(1, Integer.class).matches(2));
    }

    @AfterEach
    void clean() {
        SampleRepository.getInstance().clear();
    }

    private void addMethodCall(List<PersistentMethodCall> persistentMethodCallList, List<Object> params, Object returnValue) {
        PersistentParameter persistentParameter = mock(PersistentParameter.class);
        PersistentMethodCall persistentMethodCall = mock(PersistentMethodCall.class);
        persistentMethodCallList.add(persistentMethodCall);
        when(persistentMethodCall.getPersistentParameter()).thenReturn(persistentParameter);
        when(persistentMethodCall.getPersistentReturnValue()).thenReturn(returnValue);
        when(persistentParameter.getParameter()).thenReturn(params);
    }

    private static class TestService {
        boolean call(TestBean testBean, Integer i) {
            return false;
        }
    }

    private static class TestBean {
        private ComplicatedBean complicatedBean;
        private String someString;
        private Double someDouble;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestBean testBean = (TestBean) o;
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
