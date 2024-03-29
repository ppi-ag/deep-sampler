/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;

/**
 * A Service that will be instrumented to test the interceptors.
 */
public class TestService {

    public static final String HARD_CODED_RETURN_VALUE = "Some value";

    private int counter = 0;
    private Ship shipEnum;

    /**
     * This method returns a primitive parameter if the method is not sampled.
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
     * This method returns a non primitive parameter containing a byte[] if the method is not mocked.
     * This Method is intended to be used in positive and negative tests.
     *
     * @param someObject The parameter that will be returned unchanged
     * @return the unchanged parameter value
     */
    public TestBeanWithBytes echoParameter(final TestBeanWithBytes someObject) {
        return someObject;
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
    public Date testRandomSqlDate(final RecTestBean someObject) {
        return new Date((long)(Math.random() * 100000000000L));
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
        // Usually it would be better to use Collections.singletonList() for Lists with only one entry, but
        // we are unable to deserialize Collections.SingletonList since this is a private inner class.
        return Arrays.asList(new TestBean(HARD_CODED_RETURN_VALUE));
    }

    public CustomList getCustomListOfTestBeans() {
        CustomList customList = new CustomList();
        customList.add(new TestBean(HARD_CODED_RETURN_VALUE));
        return customList;
    }

    public List<String> getListOfStrings() {
        // Usually it would be better to use Collections.singletonList() for Lists with only one entry, but
        // we are unable to deserialize Collections.SingletonList since this is a private inner class.
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


    public byte[] getRandomByteArray(int size){

        byte[] b = new byte[size];
        new Random().nextBytes(b);
        return b;

    }

    public Animal getConcreteDogObject() {
        return new Dog("Porthos");
    }

    public Dog getSubClassOfDog() {
        return new Beagle("Porthos");
    }

    public AbstractDog getSubClassOfAbstractDog() {
        return new Labrador("BlackDog");
    }

    public AbstractDog getInternalClassThatExtendsAbstractDog() {
        return new AbstractDog.InternalDog("InnerClassDog");
    }

    public Animal getCatWithMouse() {
        Mouse mouse = new Mouse("Jerry");
        HunterCat cat = new HunterCat("Tom");
        cat.setFood(mouse);

        return cat;
    }

    public Dog getGenericSubClass() {
        GenericBeagle<Cheese> porthos = new GenericBeagle<>("GreedyPorthos");
        porthos.setFood(new Cheese("Cheddar"));

        return porthos;
    }

    public GenericBeagle<Cheese> getGenericClass() {
        GenericBeagle<Cheese> porthos = new GenericBeagle<>("GenericPorthos");
        porthos.setFood(new Cheese("Gauda"));

        return porthos;
    }

    public String getNull() {
        return null;
    }

    public String getShipsRegistrationFromEnum(Ship ship) {
        return ship.getRegistration();
    }

    public Ship getShipEnum() {
        return this.shipEnum;
    }

    public void setShipEnum(Ship shipEnum) {
        this.shipEnum = shipEnum;
    }

    public TestBeanWithEnum getBeanWithShipEnum() {
        return new TestBeanWithEnum(this.shipEnum);
    }

    public RetentionPolicy getEnumWithDefaultConstructor() {
        return RetentionPolicy.CLASS;
    }

    public Optional<String> getOptionalValue() {
        return Optional.of("Some optional value");
    }
}
