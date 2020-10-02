package org.deepmock.core.api;

import org.deepmock.core.internal.ProxyFactory;
import org.deepmock.core.internal.handler.VerifyBehaviorHandler;
import org.deepmock.core.model.BehaviorRepository;

/**
 * @author Jan Schankin, Rico Schrage
 */
public class Personality {

    public static void define(PersonalityTraits traits) {
        traits.define();
    }

    public static <T> TraitBuilder<T> hasTrait(T obj) {
        return new TraitBuilder<>(obj,
                BehaviorRepository.getInstance().getCurrentBehavior());
    }

    public static <T> T verifyTrait(Class<T> cls, Quantity quantity) {
        return ProxyFactory.createProxy(cls, new VerifyBehaviorHandler(quantity, cls));
    }

}
