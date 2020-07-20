package org.deepmock.core.api;

import org.deepmock.core.model.Behavior;
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
        return null;
    }
}
