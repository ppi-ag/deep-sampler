package org.deepmock.provider.standalone.behavior;

import javassist.NotFoundException;
import org.deepmock.provider.standalone.aop.AopClassLoader;
import org.deepmock.provider.standalone.aop.InterceptorBridge;
import org.deepmock.provider.standalone.aop.StandAloneAopException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

public class DeepMockExtension implements TestInstanceFactory {


    private AopClassLoader classLoader;

    public DeepMockExtension() {
        InterceptorBridge.registerMethodInterceptor(new StandAloneBehaviorInterceptor());
        try {
            classLoader = new AopClassLoader();
        } catch (NotFoundException e) {
            throw new StandAloneAopException("Unable to initialize standalone aop for DeepMock.", e);
        }
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) throws TestInstantiationException {
        String testClassName = factoryContext.getTestClass().getName();
        try {
            Class<?> instrumentedTestClass = classLoader.findClass(testClassName);

            return instrumentedTestClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new StandAloneAopException("Unable to initialize standalone aop for DeepMock.", e);
        }
    }

}
