/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;

/**
 * A Service that will be instrumented to test the interceptors.
 */
public class TestService {

    public static final String HARD_CODED_RETURN_VALUE = "Some value";

    private int counter = 0;

    /**
     * This method returns a primitve parameter if the method is not sampled.
     * This Method is intended to be used in positive and negative tests.
     *
     * @param param The parameter that will be returned unchanged
     * @return the unchanged parameter value
     */
    public String echoParameter(final String param) {
        return param;
    }

    public String anotherMethodThatReturnsStrings(final String param) {
        return param;
    }


    public String getRandom(String param) {
        return Double.toString(Math.random());
    }


    /**
     * A method that will always return -1 if it is not sampled
     * @return always -1
     */
    public int getMinusOne() {
        return -1;
    }

    /**
     * This method returns a non primitive parameter if the method is not mocked.
     * This Method is intended to be used in positive and negative tests.
     *
     * @param someObject The parameter that will be returned unchanged
     * @return the unchanged parameter value
     */
    public TestBean echoParameter(final TestBean someObject) {
        return someObject;
    }

    /**
     * We expect that DeepSampler throws an Exception if the {@link de.ppi.deepsampler.core.api.Matchers.EqualsMatcher}
     * is used, although the parameter doesn't implement equals(). This method is used to test this expectation.
     *
     * @param someObjectWithoutEquals a parameter without an equals()-method
     * @return the unchanged parameter
     */
    public TestBeanWithoutEquals echoParameter(final TestBeanWithoutEquals someObjectWithoutEquals) {
        return someObjectWithoutEquals;
    }

    /**
     * This method is needed to test whether calls of void methods can be verified or not.
     *
     * @param someInt int value
     */
    @SuppressWarnings("unused")
    public void noReturnValue(final int someInt) {
        // There is nothing to do here, we are only interested in the method call itself.
    }

    @SuppressWarnings("unused")
    public Date testSqlDate(final RecTestBean someObject) {
        return new Date(1);
    }


    @SuppressWarnings("unused")
    public String methodWithTwoParameter(final String parameterOne, final String parameterTwo) {
        return HARD_CODED_RETURN_VALUE;
    }

    @SuppressWarnings("unused")
    public String methodWithThreeParametersReturningLast(final String parameterOne, final String parameterTwo, final String parameterThree) {
        return parameterThree;
    }

    public LocalDateTime testLocalDateTime() {
        return LocalDateTime.of(2020, 10, 29, 10, 10, 10);
    }

    public String throwsException() throws TestException {
        // The Exception must be thrown by the Answer (Stub)
        return null;
    }

    public void voidThrowsException() throws TestException {
        // The Exception must be thrown by the Answer (Stub)
    }

    public int getCounter() {
        return counter;
    }

    public void incrementCounter() {
        counter++;
    }

    public void setCounter(final int counter) {
        this.counter = counter;
    }

    public String[] getArrayOfStrings() {
        return new String[]{HARD_CODED_RETURN_VALUE};
    }

    public String[][] getArrayOfStrings2d() {
        return new String[][]{new String[]{HARD_CODED_RETURN_VALUE}};
    }

    public TestBean[][][] getArrayOfTestBeans3d() {
        return new TestBean[][][] {{{new TestBean(HARD_CODED_RETURN_VALUE)}, {new TestBean(HARD_CODED_RETURN_VALUE)}}};
    }

    public TestBean[] getArrayOfTestBeans() {
        return new TestBean[]{new TestBean(HARD_CODED_RETURN_VALUE)};
    }

    public List<TestBean> getListOfTestBeans() {
        return Arrays.asList(new TestBean(HARD_CODED_RETURN_VALUE));
    }

    public List<String> getListOfStrings() {
        return Arrays.asList(HARD_CODED_RETURN_VALUE);
    }

    public Set<String> getSetOfStrings() {
        Set<String> set = new HashSet<>();
        set.add(HARD_CODED_RETURN_VALUE);

        return set;
    }

    public Set<TestBean> getSetOfTestBeans() {
        Set<TestBean> set = new HashSet<>();
        set.add(new TestBean(HARD_CODED_RETURN_VALUE));

        return set;
    }

    public Map<String, String> getMapOfStrings() {
        Map<String, String> map = new HashMap<>();
        map.put(HARD_CODED_RETURN_VALUE, HARD_CODED_RETURN_VALUE);

        return map;
    }

    public Map<String, TestBean> getMapOfStringsToTestBeans() {
        Map<String, TestBean> map = new HashMap<>();
        map.put(HARD_CODED_RETURN_VALUE, new TestBean(HARD_CODED_RETURN_VALUE));

        return map;
    }

    public Map<TestBean, TestBean> getComplexMap() {
        Map<TestBean, TestBean> map = new HashMap<>();

        map.put(new TestBean(HARD_CODED_RETURN_VALUE), new TestBean(HARD_CODED_RETURN_VALUE));

        return map;
    }

    public Map<Integer, Integer> getMapOfIntegers() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 2);

        return map;
    }
}
