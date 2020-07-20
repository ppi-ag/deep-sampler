package org.deepmock.core.api;

import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BehaviorsTest {

    @Test
    public void testBehaviorDefinitionWithoutParam() {
        // GIVEN WHEN
        Quantity quantityBehavior = Behaviors.of(Quantity.class);
        Personality.hasTrait(quantityBehavior.getTimes()).returning(4);

        // THEN
        Behavior currentBehavior = BehaviorRepository.getInstance().getCurrentBehavior();
        assertEquals(Quantity.class, currentBehavior.getJoinPoint().getTarget());
        assertTrue(currentBehavior.getParameter().isEmpty());
        assertEquals(4, currentBehavior.getReturnValueSupplier().supply());
    }
}