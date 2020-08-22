package org.deepmock.core.internal.handler;

import org.deepmock.core.api.Matchers;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.JoinPoint;
import org.deepmock.core.model.ParameterMatcher;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RecordBehaviorHandler extends ReturningBehaviorHandler {
    private final Class<?> cls;

    public RecordBehaviorHandler(Class<?> cls) {
        this.cls = cls;
    }

    @Override
    public Object invoke(Object self, Method method, Method proceed, Object[] args) {
        Behavior behavior = createBehavior(cls, method, args);

        BehaviorRepository.getInstance().add(behavior);

        return createEmptyProxy(method.getReturnType());
    }


}
