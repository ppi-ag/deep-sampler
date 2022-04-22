/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

import de.ppi.deepsampler.core.api.FixedQuantity;
import de.ppi.deepsampler.core.api.PersistentSample;
import de.ppi.deepsampler.core.api.Sample;
import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.error.NoMatchingParametersFoundException;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.persistence.api.PersistentMatchers;
import de.ppi.deepsampler.persistence.api.PersistentSampleManager;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.json.JsonSourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.RetentionPolicy;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDateTime;

import static de.ppi.deepsampler.core.api.FixedQuantity.ONCE;
import static de.ppi.deepsampler.core.api.Matchers.any;
import static de.ppi.deepsampler.core.api.Matchers.anyInt;
import static de.ppi.deepsampler.core.api.Matchers.anyString;
import static de.ppi.deepsampler.core.api.Matchers.equalTo;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.anyRecorded;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.combo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This TestClass must be be used to test all aop-provider in order to ensure that all providers would support the same
 * functionality.
 */
@SuppressWarnings("java:S5960")
@ExtendWith(TempJsonFile.class)
public abstract class PersistentSamplerAspectTest {

    public static final String VALUE_A = "Value A";
    public static final String VALUE_B = "Value B";
    private static final TestBean TEST_BEAN_A = new TestBean();
    public static final String MY_ECHO_PARAMS = "MY ECHO PARAMS";
    public static final String NO_RETURN_VALUE_SAMPLE_ID = "NoReturnValue";
    public static final String BLOCK = "BLOCK";


    /**
     * The {@link TestService} is a Service that is used to test method interception by a SamplerInterceptor. Since this class must be
     * instantiated by the concrete Dependency Injection Framework, the creation of this instance must be done by the concrete TestCase.
     *
     * @return An instance of {@link TestService} that has been created in a way that enables method interception by a particular AOP-framework (i.e. Spring).
     */
    public abstract TestService getTestService();


    @BeforeEach
    public void cleanUp() {
        Sampler.clear();
    }


    @Test
    public void samplesCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter(PersistentMatchers.anyRecordedString()));
        PersistentSample.of(testServiceSampler.echoParameter(PersistentMatchers.anyRecorded(TestBean.class)));

        getTestService().echoParameter(VALUE_A);
        getTestService().echoParameter(TEST_BEAN_A);

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.echoParameter(PersistentMatchers.anyRecordedString()));
        PersistentSample.of(testServiceSampler.echoParameter(PersistentMatchers.anyRecorded(TestBean.class)));
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertNotNull(getTestService().echoParameter(VALUE_A));
        assertNotNull(getTestService().echoParameter(TEST_BEAN_A));
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));

        assertThrows(NoMatchingParametersFoundException.class, () -> getTestService().echoParameter(VALUE_B));
    }


    @Test
    public void voidMethodsCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.forVerification(testServiceSampler).noReturnValue(anyInt());

        getTestService().noReturnValue(2);
        getTestService().noReturnValue(3);

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.forVerification(testServiceSampler).noReturnValue(anyInt());
        source.load();

        getTestService().noReturnValue(2);
        getTestService().noReturnValue(3);

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sample.verifyCallQuantity(TestService.class, ONCE).noReturnValue(2);
    }

    @Test
    public void nullSampleCanBeRecordedAndLoaded(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getNull());

        getTestService().getNull();

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        // WHEN
        PersistentSample.of(testServiceSampler.getNull());
        source.load();
        assertFalse(SampleRepository.getInstance().isEmpty());

        final String actualValue = getTestService().getNull();

        // THEN
        assertNull(actualValue);
    }

    @Test
    public void sqlDateCanBeRecordedAndLoaded(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C')));

        final Date expectedDate = getTestService().testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C'));

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        // WHEN
        PersistentSample.of(testServiceSampler.testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C')));
        source.load();

        // THEN
        assertFalse(SampleRepository.getInstance().isEmpty());

        final Date stubbedDate = getTestService().testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C'));
        assertEquals(expectedDate, stubbedDate);

        assertThat(tempFile).content().containsPattern("\"returnValue\" : \\[ \"java.sql.Date\", [0-9]+ \\]");
    }

    @Test
    public void sqlDateCanBeRecordedAndLoadedWithBeanConverter(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C')));

        final Date expectedDate = getTestService().testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C'));

        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder()
                .buildWithFile(tempFile))
                .addBeanExtension(new SqlDateBeanConverterExtension());

        source.recordSamples();

        clearSampleRepositoryWithAssertion();

        // WHEN
        PersistentSample.of(testServiceSampler.testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C')));
        source.load();

        // THEN
        assertFalse(SampleRepository.getInstance().isEmpty());

        final Date stubbedDate = getTestService().testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C'));
        assertEquals(expectedDate.toString(), stubbedDate.toString());

        assertThat(tempFile).content().containsPattern("\"returnValue\" : \"[0-9]{2}.[0-9]{2}.[0-9]{4}");
    }

    @Test
    public void listOfTestBeansReturnValueCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getListOfTestBeans());

        getTestService().getListOfTestBeans();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getListOfTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getListOfTestBeans().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getListOfTestBeans().get(0).getValue());
    }

    @Test
    public void customListWithoutGenericsIsNotAllowed(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getCustomListOfTestBeans());

        getTestService().getCustomListOfTestBeans();
        final PersistenceException expectedException = assertThrows(PersistenceException.class, () -> save(tempFile));

        assertEquals("CollectionExtension is only able to serialize subtypes of Collections, that declare exactly one generic type parameter. " +
                "de.ppi.deepsampler.provider.common.CustomList does not have any generic type parameters. The type parameter is necessary to detect " +
                "the type of the objects inside of the Collection. de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension's can be used to " +
                "tell DeepSampler, how to de/serialize beans, that cannot be serialized by DeepSampler out of the box.", expectedException.getMessage());

    }


    @Test
    public void listOfStringsReturnValueCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getListOfStrings());

        getTestService().getListOfStrings();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getListOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getListOfStrings().size());
    }

    @Test
    public void setOfStringsReturnValueCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getSetOfStrings());

        getTestService().getSetOfStrings();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getSetOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getSetOfStrings().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getSetOfStrings().toArray()[0]);
    }

    @Test
    public void setOfTestBeansReturnValueCanBeRecordedAndLoaded(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getSetOfTestBeans());

        // WHEN
        getTestService().getSetOfTestBeans();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getSetOfTestBeans());

        source.load();

        // THEN
        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getSetOfTestBeans().size());

        assertThat(getTestService().getSetOfTestBeans().stream().findFirst())
                .isPresent()
                .map(TestBean::getValue).hasValue(TestService.HARD_CODED_RETURN_VALUE);
    }

    @Test
    public void arrayOfTestBeansReturnValueCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfTestBeans());

        getTestService().getArrayOfTestBeans();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getArrayOfTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getArrayOfTestBeans().length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getArrayOfTestBeans()[0].getValue());
    }

    @Test
    public void arrayOfStringsReturnValueCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfStrings());

        getTestService().getArrayOfStrings();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getArrayOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getArrayOfStrings().length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getArrayOfStrings()[0]);
    }

    @Test
    void multidimensionalArrayOfStringsCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfStrings2d());

        getTestService().getArrayOfStrings2d();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getArrayOfStrings2d());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        final String[][] result = getTestService().getArrayOfStrings2d();

        // THEN
        assertEquals(1, result.length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, result[0][0]);
    }


    @Test
    void multidimensionalArrayOfTestBeansCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfTestBeans3d());

        getTestService().getArrayOfTestBeans3d();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getArrayOfTestBeans3d());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        final TestBean[][][] result = getTestService().getArrayOfTestBeans3d();

        // THEN
        assertEquals(1, result.length);
        assertEquals(2, result[0].length);
        assertEquals(1, result[0][0].length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, result[0][0][0].getValue());
    }

    @Test
    public void mapOfStringsReturnValueCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getMapOfStrings());

        getTestService().getMapOfStrings();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getMapOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfStrings().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getMapOfStrings().get(TestService.HARD_CODED_RETURN_VALUE));
    }

    @Test
    public void mapOfStringsToTestBeansReturnValueCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getMapOfStringsToTestBeans());

        getTestService().getMapOfStringsToTestBeans();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getMapOfStringsToTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfStringsToTestBeans().size());
        assertNotNull(getTestService().getMapOfStringsToTestBeans().get(TestService.HARD_CODED_RETURN_VALUE));

        final TestBean loadedTestBean = getTestService().getMapOfStringsToTestBeans().get(TestService.HARD_CODED_RETURN_VALUE);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, loadedTestBean.getValue());
    }

    @Test
    public void mapOfIntegersReturnValueCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getMapOfIntegers());

        getTestService().getMapOfIntegers();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getMapOfIntegers());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfIntegers().size());
        assertEquals(2, getTestService().getMapOfIntegers().get(1));
    }

    @Test
    public void optionalValueCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getOptionalValue()).hasId("getOptionalValue");

        getTestService().getOptionalValue();

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getOptionalValue()).hasId("getOptionalValue");
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertThat(getTestService().getOptionalValue())
                .isPresent()
                .hasValue("Some optional value");
    }


    @Test
    public void callsWithNotMatchingParametersAreRoutedToOriginalMethod(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getRandom(VALUE_A));

        final String hopefullyRecordedValue = getTestService().getRandom(VALUE_A);

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getRandom(VALUE_A));
        Sample.of(testServiceSampler.getRandom(anyString())).callsOriginalMethod();
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(hopefullyRecordedValue, getTestService().getRandom(VALUE_A));
        assertNotEquals(hopefullyRecordedValue, getTestService().getRandom(VALUE_B));
    }

    @Test
    public void manualIdSetForRecordingAndLoadingNoCorrectDef(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId("MY WRONG ECHO PARAMS");
        assertThrows(PersistenceException.class,
                source::load);
    }

    @Test
    public void manualIdSetForRecordingAndLoadingCorrectDef(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals("ABC", getTestService().echoParameter("ABC"));
    }

    @Test
    public void manualIdSetForRecordingAndLoadingCorrectDefVoidMethod(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.forVerification(testServiceSampler).noReturnValue(2);
        PersistentSample.setIdToLastMethodCall(NO_RETURN_VALUE_SAMPLE_ID);

        getTestService().noReturnValue(2);

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.forVerification(testServiceSampler).noReturnValue(2);
        PersistentSample.setIdToLastMethodCall(NO_RETURN_VALUE_SAMPLE_ID);
        source.load();
        getTestService().noReturnValue(2);

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(NO_RETURN_VALUE_SAMPLE_ID, SampleRepository.getInstance().getSamples().get(0).getSampleId());
        Sample.verifyCallQuantity(TestService.class, new FixedQuantity(1)).noReturnValue(2);
    }

    @Test
    public void localDateTimeCanBeRecordedAndLoaded(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.testLocalDateTime());

        getTestService().testLocalDateTime();

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.testLocalDateTime());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(LocalDateTime.of(2020, 10, 29, 10, 10, 10), getTestService().testLocalDateTime());
    }

    @Test
    public void enumInParameterCanBeRecordedAndLoaded(final Path tempFile) {
        // 👉 GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getShipsRegistrationFromEnum(any(Ship.class)));

        getTestService().getShipsRegistrationFromEnum(Ship.ENTERPRISE);

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        // 🧪 WHEN
        PersistentSample.of(testServiceSampler.getShipsRegistrationFromEnum(any(Ship.class)));
        source.load();
        // 🔬 THEN
        assertThrows(NoMatchingParametersFoundException.class, ()-> getTestService().getShipsRegistrationFromEnum(Ship.DEFIANT));
        final String actualRegistration = getTestService().getShipsRegistrationFromEnum(Ship.ENTERPRISE);
        assertEquals(Ship.ENTERPRISE.getRegistration(), actualRegistration);
    }


    @Test
    public void enumInReturnValueCanBeRecordedAndLoaded(final Path tempFile) {
        // 👉 GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getShipEnum());

        final Ship recordedShip = Ship.ENTERPRISE;
        getTestService().setShipEnum(recordedShip);

        getTestService().getShipEnum();

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        // 🧪 WHEN
        PersistentSample.of(testServiceSampler.getShipEnum());
        source.load();

        final Ship notShip = Ship.DEFIANT;
        getTestService().setShipEnum(notShip);

        final Ship actualShip = getTestService().getShipEnum();

        // 🔬 THEN
        assertEquals(recordedShip, actualShip);
    }


    @Test
    public void enumInBeanCanBeRecordedAndLoaded(final Path tempFile) {
        // 👉 GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getBeanWithShipEnum());

        final Ship recordedShip = Ship.ENTERPRISE;
        getTestService().setShipEnum(recordedShip);

        getTestService().getBeanWithShipEnum();

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        // 🧪 WHEN
        PersistentSample.of(testServiceSampler.getBeanWithShipEnum());
        source.load();

        final Ship notShip = Ship.DEFIANT;
        getTestService().setShipEnum(notShip);

        final TestBeanWithEnum actualBeanWithShipEnum = getTestService().getBeanWithShipEnum();

        // 🔬 THEN
        assertEquals(recordedShip, actualBeanWithShipEnum.getShip());
    }


    @Test
    public void enumWithDefaultConstructorCanBeRecordedAndLoaded(final Path tempFile) {
        // 👉 GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getEnumWithDefaultConstructor());

        getTestService().getEnumWithDefaultConstructor();

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        // 🧪 WHEN
        PersistentSample.of(testServiceSampler.getEnumWithDefaultConstructor());
        source.load();

        final RetentionPolicy actualEnum = getTestService().getEnumWithDefaultConstructor();

        // 🔬 THEN
        assertEquals(RetentionPolicy.CLASS, actualEnum);
    }


    @Test
    void testComboMatcherLoadAllButAcceptOnlyA(final Path tempFile) {
        // 👉 GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter(anyString())).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");

        final PersistentSampleManager source = save(tempFile);
        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.echoParameter(combo(anyString(), (f, s) -> f.equals("A")))).hasId(MY_ECHO_PARAMS);

        source.load();

        // 🧪 WHEN
        final TestService testService = getTestService();
        final String result = testService.echoParameter("A");
        final String secondCallResult = testService.echoParameter("A");

        // 🔬 THEN
        assertEquals("ABC", result);
        assertEquals("ABC", secondCallResult);
        assertThrows(NoMatchingParametersFoundException.class, () -> testService.echoParameter("B"));
    }

    @Test
    void testPersistentMatcherLoadAllButAcceptOnlyA(final Path tempFile) {
        // 👉 GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter(anyString())).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");

        final PersistentSampleManager source = save(tempFile);
        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.echoParameter((String) anyRecorded((f, s) -> f.equals("A")))).hasId(MY_ECHO_PARAMS);

        source.load();

        // 🧪 WHEN
        final TestService testService = getTestService();
        final String result = testService.echoParameter("A");
        final String secondCallResult = testService.echoParameter("A");

        // 🔬 THEN
        assertEquals("ABC", result);
        assertEquals("ABC", secondCallResult);
        assertThrows(NoMatchingParametersFoundException.class, () -> testService.echoParameter("B"));
    }

    @Test
    void testComboMatcherSecondArgument(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.methodWithThreeParametersReturningLast(anyString(), anyString(), anyString())).hasId(MY_ECHO_PARAMS);

        getTestService().methodWithThreeParametersReturningLast(BLOCK, "B", "R1");
        getTestService().methodWithThreeParametersReturningLast(BLOCK, "C", "R3");

        final PersistentSampleManager source = save(tempFile);
        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.methodWithThreeParametersReturningLast(equalTo(BLOCK), combo(anyString(), (f, s) -> f.equals("B")), combo(anyString(), (f, s) -> true))).hasId(MY_ECHO_PARAMS);
        source.load();

        // WHEN
        final TestService testService = getTestService();
        final String result = testService.methodWithThreeParametersReturningLast(BLOCK, "B", "ABC2");

        // THEN
        assertThrows(NoMatchingParametersFoundException.class, () -> testService.methodWithThreeParametersReturningLast(BLOCK, "C", "ABC1"));
        assertEquals("R1", result);
    }

    @Test
    void testPersistentMatcherSecondArgument(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.methodWithThreeParametersReturningLast(anyString(), anyString(), anyString())).hasId(MY_ECHO_PARAMS);

        getTestService().methodWithThreeParametersReturningLast(BLOCK, "B", "R1");
        getTestService().methodWithThreeParametersReturningLast(BLOCK, "C", "R3");

        final PersistentSampleManager source = save(tempFile);
        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.methodWithThreeParametersReturningLast(equalTo(BLOCK), anyRecorded((f, s) -> f.equals("B")), anyRecorded((f, s) -> true))).hasId(MY_ECHO_PARAMS);
        source.load();

        // WHEN
        final TestService testService = getTestService();
        final String result = testService.methodWithThreeParametersReturningLast(BLOCK, "B", "ABC2");

        // THEN
        assertThrows(NoMatchingParametersFoundException.class, () -> testService.methodWithThreeParametersReturningLast(BLOCK, "C", "ABC1"));
        assertEquals("R1", result);
    }

    @Test
    public void equalsMatcherComplainsWhenParameterHasNoEqualsMethod(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter(any(TestBeanWithoutEquals.class)));

        getTestService().echoParameter(new TestBeanWithoutEquals());

        final PersistentSampleManager source = save(tempFile);
        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.echoParameter(any(TestBeanWithoutEquals.class)));
        source.load();

        // WHEN
        final TestService testService = getTestService();

        assertThatExceptionOfType(InvalidConfigException.class)
                .isThrownBy(() -> testService.echoParameter(new TestBeanWithoutEquals()))
                .withMessage("The class de.ppi.deepsampler.provider.common.TestBeanWithoutEquals must implement equals() " +
                        "if you want to use an de.ppi.deepsampler.core.api.Matchers$EqualsMatcher");
    }

    @Test
    public void byteArrayCanBeRecordedAndLoaded(final Path tempFile) {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getRandomByteArray(anyInt()));

        final byte[] expectedArray = getTestService().getRandomByteArray(42);

        final PersistentSampleManager source = save(tempFile);
        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getRandomByteArray(anyInt()));

        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        final byte[] valueStubbedMethod = getTestService().getRandomByteArray(42);
        assertArrayEquals(expectedArray, valueStubbedMethod);
    }

    @Test
    public void mixPureJavaApiAndPersistenceApi(final Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.testLocalDateTime());

        getTestService().testLocalDateTime();

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        // MIX persistence definition and pure java definition
        PersistentSample.of(testServiceSampler.testLocalDateTime());
        Sample.of(testServiceSampler.echoParameter("ABC")).is("CBD");
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(LocalDateTime.of(2020, 10, 29, 10, 10, 10), getTestService().testLocalDateTime());
        assertEquals("CBD", getTestService().echoParameter("ABC"));
    }

    @Test
    public void interfaceImplementationCanBeRecordedAndLoaded(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getConcreteDogObject());

        // make the method call that is recorded
        getTestService().getConcreteDogObject();

        // save the recorded sample to file...
        final PersistentSampleManager source = save(tempFile);

        // Reset SampleRepository to ensure that only deserialized samples will be used in the next steps...
        clearSampleRepositoryWithAssertion();

        // deserialize the sample...
        PersistentSample.of(testServiceSampler.getConcreteDogObject());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        final Animal actualDog = getTestService().getConcreteDogObject();

        // THEN
        assertNotNull(actualDog);
        // Although only the interface Animal was declared in the sampled method, we expect to see the concrete return type Dog:
        assertThat(actualDog).isInstanceOf(Dog.class);
        assertEquals("Porthos", actualDog.getName());
    }

    @Test
    public void subclassFromAConcreteClassCanBeRecordedAndLoaded(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getSubClassOfDog());

        // make the method call that is recorded
        getTestService().getSubClassOfDog();

        // save the recorded sample to file...
        final PersistentSampleManager source = save(tempFile);

        // Reset SampleRepository to ensure that only deserialized samples will be used in the next steps...
        clearSampleRepositoryWithAssertion();

        // deserialize the sample...
        PersistentSample.of(testServiceSampler.getSubClassOfDog());
        source.load();
        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        final Dog actualBeagle = getTestService().getSubClassOfDog();

        // THEN

        // Although only the parent type Dog was declared in the sampled method, we expect to see the return type Beagle:
        assertThat(actualBeagle).isInstanceOf(Beagle.class);
        assertEquals("Porthos", actualBeagle.getName());
    }

    @Test
    public void subclassFromAnAbstractClassCanBeRecordedAndLoaded(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getSubClassOfAbstractDog());

        // make the method call that is recorded
        getTestService().getSubClassOfAbstractDog();

        // save the recorded sample to file...
        final PersistentSampleManager source = save(tempFile);

        // Reset SampleRepository to ensure that only deserialized samples will be used in the next steps...
        clearSampleRepositoryWithAssertion();

        // deserialize the sample...
        PersistentSample.of(testServiceSampler.getSubClassOfAbstractDog());
        source.load();
        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        final AbstractDog actualLabrador = getTestService().getSubClassOfAbstractDog();

        // THEN

        // Although only the abstract parent type AbstractDog was declared in the sampled method, we expect to see the
        // concrete return type Labrador:
        assertThat(actualLabrador).isInstanceOf(Labrador.class);
        assertEquals("BlackDog", actualLabrador.getName());
    }

    @Test
    public void polymorphicInnerClassCanBeRecordedAndLoaded(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getInternalClassThatExtendsAbstractDog());

        // make the method call that is recorded
        getTestService().getInternalClassThatExtendsAbstractDog();

        // save the recorded sample to file...
        final PersistentSampleManager source = save(tempFile);

        // Reset SampleRepository to ensure that only deserialized samples will be used in the next steps...
        clearSampleRepositoryWithAssertion();

        // deserialize the sample...
        PersistentSample.of(testServiceSampler.getInternalClassThatExtendsAbstractDog());
        source.load();
        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        final AbstractDog actualInternalDog = getTestService().getInternalClassThatExtendsAbstractDog();

        // THEN

        // Although only the abstract parent type AbstractDog was declared in the sampled method, we expect to see the
        // concrete inner class AbstractDog.InternalDog:
        assertThat(actualInternalDog).isInstanceOf(AbstractDog.InternalDog.class);
        assertEquals("InnerClassDog", actualInternalDog.getName());
    }

    @Test
    public void subClassWithReferencedObjectCanBeRecordedAndLoaded(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getCatWithMouse());

        // make the method call that is recorded
        getTestService().getCatWithMouse();

        // save the recorded sample to file...
        final PersistentSampleManager source = save(tempFile);

        // Reset SampleRepository to ensure that only deserialized samples will be used in the next steps...
        clearSampleRepositoryWithAssertion();

        // deserialize the sample...
        PersistentSample.of(testServiceSampler.getCatWithMouse());
        source.load();
        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        final Animal actualCatWithMouse = getTestService().getCatWithMouse();

        // THEN

        // Although only the abstract parent type AbstractDog was declared in the sampled method, we expect to see the
        // concrete inner class AbstractDog.InternalDog:
        assertThat(actualCatWithMouse).isInstanceOf(HunterCat.class);
        assertEquals("Tom", actualCatWithMouse.getName());

        final HunterCat actualHunterCat = (HunterCat) actualCatWithMouse;

        assertThat(actualHunterCat.getFood()).isInstanceOf(Mouse.class);
        assertThat(actualHunterCat.getFood().getName()).isEqualTo("Jerry");
    }

    @Test
    public void genericReferencedClassCanBeRecordedAndLoaded(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getGenericSubClass());

        // make the method call that is recorded
        getTestService().getGenericSubClass();

        // save the recorded sample to file...
        final PersistentSampleManager source = save(tempFile);

        // Reset SampleRepository to ensure that only deserialized samples will be used in the next steps...
        clearSampleRepositoryWithAssertion();

        // deserialize the sample...
        PersistentSample.of(testServiceSampler.getGenericSubClass());
        source.load();
        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        final Dog expectedGenericType = getTestService().getGenericSubClass();

        // THEN

        // Although only the abstract parent type AbstractDog was declared in the sampled method, we expect to see the
        // concrete inner class AbstractDog.InternalDog:
        assertThat(expectedGenericType).isInstanceOf(GenericBeagle.class);
        assertEquals("GreedyPorthos", expectedGenericType.getName());

        final GenericBeagle<?> actualGenericBeagle = (GenericBeagle<?>) expectedGenericType;

        assertThat(actualGenericBeagle.getFood()).isInstanceOf(Cheese.class);
        assertThat(((Cheese) actualGenericBeagle.getFood()).getName()).isEqualTo("Cheddar");
    }

    @Test
    public void genericClassCanBeRecordedAndLoaded(final Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getGenericClass());

        // make the method call that is recorded
        getTestService().getGenericClass();

        // save the recorded sample to file...
        final PersistentSampleManager source = save(tempFile);

        // Reset SampleRepository to ensure that only deserialized samples will be used in the next steps...
        clearSampleRepositoryWithAssertion();

        // deserialize the sample...
        PersistentSample.of(testServiceSampler.getGenericClass());
        source.load();
        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        final GenericBeagle<Cheese> expectedGenericType = getTestService().getGenericClass();

        // THEN
        assertEquals("GenericPorthos", expectedGenericType.getName());

        assertThat(expectedGenericType.getFood()).isInstanceOf(Cheese.class);
        assertThat(expectedGenericType.getFood().getName()).isEqualTo("Gauda");
    }

    @Test
    public void polymorphicSampleWithMissingConcreteClassThrowsError() {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getConcreteDogObject());

        final String samplerFile = "/polymorphicSampleWithMissingConcreteClassThrowsError.json";
        final JsonSourceManager jsonSourceManager = JsonSourceManager.builder().buildWithClassPathResource(samplerFile, this.getClass());
        final PersistentSampleManager source = PersistentSampler.source(jsonSourceManager);

        // WHEN
        assertThatThrownBy(source::load)
                // THEN
                .isInstanceOf(PersistenceException.class)
                .hasMessage("The Polymorphic Class de.ppi.deepsampler.provider.common.ClassDoesNotExist was not found. " +
                        "This occurs if a polymorphic class was recorded but is not in the classpath (anymore?)");
    }

    private void clearSampleRepositoryWithAssertion() {
        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());
    }

    private PersistentSampleManager save(final Path pathToFile) {
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.recordSamples();
        return source;
    }

}