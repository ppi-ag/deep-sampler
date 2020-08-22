package org.deepmock.core.api;

import org.deepmock.core.internal.FixedQuantity;
import org.deepmock.core.internal.ProxyFactory;
import org.deepmock.core.internal.handler.RecordBehaviorHandler;
import org.deepmock.core.model.BehaviorRepository;

public class Behaviors {

    public static void clear() {
        BehaviorRepository.getInstance().clear();
    }

    @SuppressWarnings("unchecked")
    public static <T> T of(Class<T> cls) {
        return ProxyFactory.createProxy(cls, new RecordBehaviorHandler(cls));
    }

    public static Quantity times(int i) {
        return new FixedQuantity(i);
    }

}
